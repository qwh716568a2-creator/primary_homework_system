package com.primaryhomework.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.primaryhomework.backend.entity.dto.WrongBookQueryDto;
import com.primaryhomework.backend.entity.dto.mobile.SubmitDto;
import com.primaryhomework.backend.entity.po.*;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.mobile.*;
import com.primaryhomework.backend.mapper.*;
import com.primaryhomework.backend.service.ParentService;
import com.primaryhomework.backend.service.WrongBookService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.CurrentUserSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Map<String, String> SUBJECT_NAME_MAP = Map.ofEntries(
            Map.entry("math", "\u6570\u5b66"),
            Map.entry("chinese", "\u8bed\u6587"),
            Map.entry("english", "\u82f1\u8bed"),
            Map.entry("science", "\u79d1\u5b66"),
            Map.entry("moral", "\u9053\u5fb7\u4e0e\u6cd5\u6cbb"),
            Map.entry("morality", "\u9053\u5fb7\u4e0e\u6cd5\u6cbb"),
            Map.entry("art", "\u7f8e\u672f"),
            Map.entry("music", "\u97f3\u4e50"),
            Map.entry("pe", "\u4f53\u80b2")
    );

    private final UserMapper userMapper;
    private final ParentMapper parentMapper;
    private final ParentStudentMapper parentStudentMapper;
    private final StudentMapper studentMapper;
    private final SchoolClassMapper schoolClassMapper;
    private final SchoolGradeMapper schoolGradeMapper;
    private final HomeworkMapper homeworkMapper;
    private final HomeworkTaskMapper homeworkTaskMapper;
    private final HomeworkAttachmentMapper homeworkAttachmentMapper;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final HomeworkSubmissionAssetMapper homeworkSubmissionAssetMapper;
    private final HomeworkReviewMapper homeworkReviewMapper;
    private final NotificationMapper notificationMapper;
    private final WrongBookService wrongBookService;

    @Override
    public List<ChildVo> listStudents(String authorization) {
        UserPo parentUser = resolveParentUser(authorization);
        List<ParentStudentPo> relations = listRelations(parentUser.getId());
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, StudentPo> studentMap = loadStudentMap(relations.stream().map(ParentStudentPo::getStudentId).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, UserPo> studentUserMap = loadUserMap(studentMap.values().stream().map(StudentPo::getStudentUserId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, SchoolClassPo> classMap = loadClassMap(studentMap.values().stream().map(StudentPo::getClassId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, SchoolGradePo> gradeMap = loadGradeMap(studentMap.values().stream().map(StudentPo::getGradeId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, TaskSummary> taskSummaryMap = loadTaskSummaryMap(studentMap.keySet());

        List<ChildVo> list = new ArrayList<>();
        for (ParentStudentPo relation : relations) {
            StudentPo student = studentMap.get(relation.getStudentId());
            if (student == null) {
                continue;
            }
            UserPo studentUser = studentUserMap.get(student.getStudentUserId());
            SchoolClassPo classPo = classMap.get(student.getClassId());
            SchoolGradePo gradePo = gradeMap.get(student.getGradeId());
            TaskSummary summary = taskSummaryMap.getOrDefault(student.getId(), new TaskSummary());
            ChildVo vo = new ChildVo();
            vo.setId(String.valueOf(student.getId()));
            vo.setName(studentUser == null ? "\u672a\u547d\u540d\u5b66\u751f" : defaultString(studentUser.getUserName(), "\u672a\u547d\u540d\u5b66\u751f"));
            vo.setClassName(classPo == null ? "" : defaultString(classPo.getClassName(), ""));
            vo.setGradeName(gradePo == null ? "" : defaultString(gradePo.getGradeName(), ""));
            vo.setPendingCount(summary.pendingCount());
            vo.setSubmittedCount(summary.submittedCount());
            vo.setRevisionCount(summary.revisionCount());
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<HomeworkVo> listHomeworks(String authorization, Long studentId, String tab) {
        UserPo parentUser = resolveParentUser(authorization);
        StudentPo student = requireBoundStudent(parentUser.getId(), studentId);
        List<HomeworkTaskPo> tasks = homeworkTaskMapper.selectList(new LambdaQueryWrapper<HomeworkTaskPo>()
                .eq(HomeworkTaskPo::getStudentId, student.getId())
                .eq(HomeworkTaskPo::getIsDeleted, false)
                .orderByDesc(HomeworkTaskPo::getUpdatedAt)
                .orderByDesc(HomeworkTaskPo::getId));
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }
        return buildHomeworkList(tasks).stream().filter(item -> matchesTab(item.getStatus(), normalizeTab(tab))).toList();
    }

    @Override
    public HomeworkVo getHomework(String authorization, Long studentId, Long homeworkId) {
        UserPo parentUser = resolveParentUser(authorization);
        StudentPo student = requireBoundStudent(parentUser.getId(), studentId);
        return buildHomeworkDetail(requireTask(student.getId(), homeworkId));
    }

    @Override
    @Transactional
    public void assistSubmit(String authorization, Long homeworkId, SubmitDto submitDto) {
        UserPo parentUser = resolveParentUser(authorization);
        StudentPo student = resolveSubmitStudent(parentUser.getId(), homeworkId, submitDto);
        HomeworkTaskPo task = requireTask(student.getId(), homeworkId);
        HomeworkPo homework = requireActiveHomework(homeworkId);
        validateSubmit(homework, task);
        saveSubmission(parentUser, task, homework, submitDto);
    }

    @Override
    public PageDTO<MessageVo> listNotifications(String authorization, String readStatus, Integer pageNo, Integer pageSize) {
        UserPo parentUser = resolveParentUser(authorization);
        List<NotificationPo> all = notificationMapper.selectList(new LambdaQueryWrapper<NotificationPo>()
                .eq(NotificationPo::getReceiverUserId, parentUser.getId())
                .eq(NotificationPo::getReceiverRole, "parent")
                .orderByDesc(NotificationPo::getSentAt)
                .orderByDesc(NotificationPo::getId));
        List<NotificationPo> filtered = all.stream().filter(item -> matchesReadStatus(item, readStatus)).toList();
        PageSlice<NotificationPo> slice = slice(filtered, pageNo, pageSize);
        Map<Long, StudentPo> studentMap = loadStudentMap(listRelations(parentUser.getId()).stream().map(ParentStudentPo::getStudentId).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, UserPo> studentUserMap = loadUserMap(studentMap.values().stream().map(StudentPo::getStudentUserId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, String> childNameMap = loadChildNameMap(slice.items(), studentMap, studentUserMap);
        return PageDTO.of(slice.items().stream().map(item -> buildMessageVo(item, childNameMap.get(item.getId()))).toList(), slice.total(), slice.pageNo(), slice.pageSize());
    }

    @Override
    @Transactional
    public MessageVo getNotification(String authorization, Long notificationId) {
        UserPo parentUser = resolveParentUser(authorization);
        NotificationPo notification = requireParentNotification(parentUser.getId(), notificationId);
        markNotificationReadIfNeeded(notification);
        Map<Long, StudentPo> studentMap = loadStudentMap(listRelations(parentUser.getId()).stream().map(ParentStudentPo::getStudentId).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, UserPo> studentUserMap = loadUserMap(studentMap.values().stream().map(StudentPo::getStudentUserId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, String> childNameMap = loadChildNameMap(List.of(notification), studentMap, studentUserMap);
        return buildMessageVo(notification, childNameMap.get(notification.getId()));
    }

    @Override
    @Transactional
    public void markNotificationRead(String authorization, Long notificationId) {
        UserPo parentUser = resolveParentUser(authorization);
        NotificationPo notification = requireParentNotification(parentUser.getId(), notificationId);
        markNotificationReadIfNeeded(notification);
    }

    @Override
    public PageDTO<WrongBookListVo> listWrongBooks(String authorization, Long studentId, WrongBookQueryDto queryDto) {
        return wrongBookService.pageParentWrongBooks(authorization, studentId, queryDto);
    }

    private UserPo resolveParentUser(String authorization) {
        return CurrentUserSupport.requireUser(authorization, "parent", userMapper);
    }

    private boolean isActiveParent(UserPo user) {
        return CurrentUserSupport.isActiveUser(user, "parent");
    }

    private NotificationPo requireParentNotification(Long parentUserId, Long notificationId) {
        if (notificationId == null) {
            throw new CommonException("\u6d88\u606fID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        NotificationPo notification = notificationMapper.selectById(notificationId);
        if (notification == null
                || !Objects.equals(notification.getReceiverUserId(), parentUserId)
                || !"parent".equalsIgnoreCase(defaultString(notification.getReceiverRole(), ""))) {
            throw new CommonException(40401, "\u6d88\u606f\u4e0d\u5b58\u5728");
        }
        return notification;
    }

    private void markNotificationReadIfNeeded(NotificationPo notification) {
        if (notification.getReadAt() != null) {
            return;
        }
        notification.setReadAt(LocalDateTime.now());
        notificationMapper.updateById(notification);
    }

    private List<ParentStudentPo> listRelations(Long parentUserId) {
        return parentStudentMapper.selectList(new LambdaQueryWrapper<ParentStudentPo>()
                .eq(ParentStudentPo::getParentUserId, parentUserId)
                .eq(ParentStudentPo::getStatus, "enabled")
                .orderByDesc(ParentStudentPo::getIsPrimary)
                .orderByAsc(ParentStudentPo::getId));
    }

    private StudentPo requireBoundStudent(Long parentUserId, Long studentId) {
        if (studentId == null) {
            throw new CommonException("studentId\u4e0d\u80fd\u4e3a\u7a7a");
        }
        long count = parentStudentMapper.selectCount(new LambdaQueryWrapper<ParentStudentPo>()
                .eq(ParentStudentPo::getParentUserId, parentUserId)
                .eq(ParentStudentPo::getStudentId, studentId)
                .eq(ParentStudentPo::getStatus, "enabled"));
        if (count <= 0) {
            throw new CommonException("\u5f53\u524d\u5bb6\u957f\u672a\u7ed1\u5b9a\u8be5\u5b66\u751f");
        }
        StudentPo student = studentMapper.selectById(studentId);
        if (student == null || !"enabled".equalsIgnoreCase(defaultString(student.getStatus(), "enabled"))) {
            throw new CommonException("\u5b66\u751f\u4e0d\u5b58\u5728\u6216\u5df2\u505c\u7528");
        }
        return student;
    }

    private StudentPo resolveSubmitStudent(Long parentUserId, Long homeworkId, SubmitDto submitDto) {
        if (submitDto != null && submitDto.getStudentId() != null) {
            return requireBoundStudent(parentUserId, submitDto.getStudentId());
        }
        List<ParentStudentPo> relations = listRelations(parentUserId);
        if (relations.isEmpty()) {
            throw new CommonException("\u5f53\u524d\u5bb6\u957f\u672a\u7ed1\u5b9a\u5b66\u751f");
        }
        Set<Long> studentIds = relations.stream().map(ParentStudentPo::getStudentId).collect(Collectors.toCollection(LinkedHashSet::new));
        List<HomeworkTaskPo> tasks = homeworkTaskMapper.selectList(new LambdaQueryWrapper<HomeworkTaskPo>()
                .eq(HomeworkTaskPo::getHomeworkId, homeworkId)
                .in(HomeworkTaskPo::getStudentId, studentIds)
                .eq(HomeworkTaskPo::getIsDeleted, false));
        if (tasks.isEmpty()) {
            throw new CommonException("\u672a\u627e\u5230\u5b69\u5b50\u5bf9\u5e94\u7684\u4f5c\u4e1a");
        }
        if (tasks.size() > 1) {
            throw new CommonException("\u5b58\u5728\u591a\u4e2a\u5b69\u5b50\u5bf9\u5e94\u540c\u4e00\u4f5c\u4e1a\uff0c\u8bf7\u4f20 studentId");
        }
        return requireBoundStudent(parentUserId, tasks.get(0).getStudentId());
    }

    private HomeworkTaskPo requireTask(Long studentId, Long homeworkId) {
        HomeworkTaskPo task = homeworkTaskMapper.selectOne(new LambdaQueryWrapper<HomeworkTaskPo>()
                .eq(HomeworkTaskPo::getStudentId, studentId)
                .eq(HomeworkTaskPo::getHomeworkId, homeworkId)
                .eq(HomeworkTaskPo::getIsDeleted, false)
                .last("limit 1"));
        if (task == null) {
            throw new CommonException("\u672a\u627e\u5230\u5bf9\u5e94\u4f5c\u4e1a");
        }
        return task;
    }

    private HomeworkPo requireActiveHomework(Long homeworkId) {
        HomeworkPo homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new CommonException("\u4f5c\u4e1a\u4e0d\u5b58\u5728");
        }
        if (!"published".equalsIgnoreCase(defaultString(homework.getStatus(), "draft"))) {
            throw new CommonException("\u5f53\u524d\u4f5c\u4e1a\u4e0d\u53ef\u63d0\u4ea4");
        }
        return homework;
    }

    private void validateSubmit(HomeworkPo homework, HomeworkTaskPo task) {
        LocalDateTime now = LocalDateTime.now();
        boolean isLate = homework.getDeadlineAt() != null && now.isAfter(homework.getDeadlineAt());
        if (isLate && !defaultBoolean(homework.getAllowLateSubmit(), false)) {
            throw new CommonException("\u4f5c\u4e1a\u5df2\u622a\u6b62\uff0c\u4e0d\u80fd\u518d\u63d0\u4ea4");
        }
        String taskStatus = defaultString(task.getTaskStatus(), "pending");
        boolean allowResubmit = defaultBoolean(homework.getAllowResubmit(), true);
        if (!allowResubmit && task.getLatestSubmissionId() != null && !"revision_required".equalsIgnoreCase(taskStatus)) {
            throw new CommonException("\u5f53\u524d\u4f5c\u4e1a\u4e0d\u5141\u8bb8\u91cd\u590d\u63d0\u4ea4");
        }
    }

    private void saveSubmission(UserPo parentUser, HomeworkTaskPo task, HomeworkPo homework, SubmitDto submitDto) {
        LocalDateTime now = LocalDateTime.now();
        boolean isLate = homework.getDeadlineAt() != null && now.isAfter(homework.getDeadlineAt());

        HomeworkSubmissionPo submission = new HomeworkSubmissionPo();
        submission.setTaskId(task.getId());
        submission.setHomeworkId(homework.getId());
        submission.setStudentId(task.getStudentId());
        submission.setOperatorRole("parent");
        submission.setOperatorUserId(parentUser.getId());
        submission.setSubmitText(trimToNull(submitDto == null ? null : submitDto.getText()));
        submission.setSubmittedAt(now);
        submission.setIsLate(isLate);
        submission.setVersionNo(defaultInteger(task.getSubmissionCount(), 0) + 1);
        submission.setSubmitStatus("submitted");
        homeworkSubmissionMapper.insert(submission);

        int sortNo = 1;
        for (String image : defaultList(submitDto == null ? null : submitDto.getImages())) {
            if (!StringUtils.hasText(image)) {
                continue;
            }
            HomeworkSubmissionAssetPo asset = new HomeworkSubmissionAssetPo();
            asset.setSubmissionId(submission.getId());
            asset.setAssetType("image");
            asset.setAssetUrl(image.trim());
            asset.setSortNo(sortNo++);
            homeworkSubmissionAssetMapper.insert(asset);
        }

        task.setLatestSubmissionId(submission.getId());
        task.setSubmissionCount(defaultInteger(task.getSubmissionCount(), 0) + 1);
        task.setLatestSubmittedAt(now);
        task.setLatestReviewStatus("unreviewed");
        task.setLatestReviewedAt(null);
        task.setIsLate(isLate);
        task.setTaskStatus("submitted");
        homeworkTaskMapper.updateById(task);
    }

    private List<HomeworkVo> buildHomeworkList(List<HomeworkTaskPo> tasks) {
        Set<Long> homeworkIds = tasks.stream().map(HomeworkTaskPo::getHomeworkId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, HomeworkPo> homeworkMap = homeworkMapper.selectList(new LambdaQueryWrapper<HomeworkPo>()
                        .in(HomeworkPo::getId, homeworkIds)
                        .orderByDesc(HomeworkPo::getPublishedAt)
                        .orderByDesc(HomeworkPo::getId))
                .stream().filter(this::isVisibleHomework)
                .collect(Collectors.toMap(HomeworkPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        if (homeworkMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, UserPo> teacherMap = loadUserMap(homeworkMap.values().stream()
                .map(HomeworkPo::getCreatorTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, List<HomeworkAttachmentPo>> attachmentMap = loadAttachmentMap(homeworkMap.keySet());
        Map<Long, HomeworkSubmissionPo> submissionMap = loadLatestSubmissionMap(tasks);
        Map<Long, List<HomeworkSubmissionAssetPo>> submissionAssetMap = loadSubmissionAssetMap(submissionMap.values().stream()
                .map(HomeworkSubmissionPo::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, HomeworkReviewPo> reviewMap = loadLatestReviewMap(tasks);

        List<HomeworkVo> list = new ArrayList<>();
        for (HomeworkTaskPo task : tasks) {
            HomeworkPo homework = homeworkMap.get(task.getHomeworkId());
            if (homework == null) {
                continue;
            }
            HomeworkSubmissionPo latestSubmission = submissionMap.get(task.getLatestSubmissionId());
            HomeworkReviewPo latestReview = reviewMap.get(task.getId());
            list.add(buildHomeworkVo(
                    task,
                    homework,
                    teacherMap.get(homework.getCreatorTeacherId()),
                    attachmentMap.get(homework.getId()),
                    latestSubmission,
                    latestSubmission == null ? Collections.emptyList() : submissionAssetMap.get(latestSubmission.getId()),
                    latestReview
            ));
        }
        return list;
    }

    private HomeworkVo buildHomeworkDetail(HomeworkTaskPo task) {
        HomeworkPo homework = requireActiveHomework(task.getHomeworkId());
        Map<Long, UserPo> teacherMap = loadUserMap(Set.of(homework.getCreatorTeacherId()));
        Map<Long, List<HomeworkAttachmentPo>> attachmentMap = loadAttachmentMap(Set.of(homework.getId()));
        HomeworkSubmissionPo latestSubmission = task.getLatestSubmissionId() == null ? null : homeworkSubmissionMapper.selectById(task.getLatestSubmissionId());
        Map<Long, List<HomeworkSubmissionAssetPo>> submissionAssetMap = latestSubmission == null ? Collections.emptyMap() : loadSubmissionAssetMap(Set.of(latestSubmission.getId()));
        HomeworkReviewPo latestReview = homeworkReviewMapper.selectList(new LambdaQueryWrapper<HomeworkReviewPo>()
                        .eq(HomeworkReviewPo::getTaskId, task.getId())
                        .orderByDesc(HomeworkReviewPo::getReviewedAt)
                        .orderByDesc(HomeworkReviewPo::getId)
                        .last("limit 1"))
                .stream().findFirst().orElse(null);
        return buildHomeworkVo(
                task,
                homework,
                teacherMap.get(homework.getCreatorTeacherId()),
                attachmentMap.get(homework.getId()),
                latestSubmission,
                latestSubmission == null ? Collections.emptyList() : submissionAssetMap.get(latestSubmission.getId()),
                latestReview
        );
    }

    private HomeworkVo buildHomeworkVo(HomeworkTaskPo task, HomeworkPo homework, UserPo teacherUser,
                                       List<HomeworkAttachmentPo> attachments, HomeworkSubmissionPo latestSubmission,
                                       List<HomeworkSubmissionAssetPo> submissionAssets, HomeworkReviewPo latestReview) {
        HomeworkVo vo = new HomeworkVo();
        vo.setId(String.valueOf(homework.getId()));
        vo.setTaskId(String.valueOf(task.getId()));
        vo.setTitle(homework.getTitle());
        vo.setSubject(subjectName(homework.getSubjectCode()));
        vo.setTeacherName(teacherUser == null ? "\u8001\u5e08" : defaultString(teacherUser.getUserName(), "\u8001\u5e08"));
        vo.setDeadline(formatTime(homework.getDeadlineAt()));
        vo.setStatus(resolveStatus(task, homework));
        vo.setSummary(buildSummary(homework.getContentText()));
        vo.setContent(defaultString(homework.getContentText(), ""));
        vo.setAllowParentAssist(true);
        vo.setAttachments(defaultList(attachments).stream().map(this::buildAttachmentVo).toList());
        vo.setSubmitTypes(splitSubmitTypes(homework.getSubmitTypeMask()));
        vo.setHasFeedback(latestReview != null);
        vo.setLatestSubmission(buildSubmissionVo(latestSubmission, submissionAssets));
        vo.setReview(buildReviewVo(latestReview));
        return vo;
    }

    private Map<Long, StudentPo> loadStudentMap(Set<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return studentMapper.selectList(new LambdaQueryWrapper<StudentPo>()
                        .in(StudentPo::getId, studentIds)
                        .eq(StudentPo::getStatus, "enabled"))
                .stream().collect(Collectors.toMap(StudentPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, UserPo> loadUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectList(new LambdaQueryWrapper<UserPo>().in(UserPo::getId, userIds))
                .stream().collect(Collectors.toMap(UserPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, SchoolClassPo> loadClassMap(Set<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolClassMapper.selectList(new LambdaQueryWrapper<SchoolClassPo>()
                        .in(SchoolClassPo::getId, classIds)
                        .eq(SchoolClassPo::getStatus, "enabled"))
                .stream().collect(Collectors.toMap(SchoolClassPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, SchoolGradePo> loadGradeMap(Set<Long> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolGradeMapper.selectList(new LambdaQueryWrapper<SchoolGradePo>().in(SchoolGradePo::getId, gradeIds))
                .stream().collect(Collectors.toMap(SchoolGradePo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, List<HomeworkAttachmentPo>> loadAttachmentMap(Set<Long> homeworkIds) {
        if (homeworkIds == null || homeworkIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return homeworkAttachmentMapper.selectList(new LambdaQueryWrapper<HomeworkAttachmentPo>()
                        .in(HomeworkAttachmentPo::getHomeworkId, homeworkIds)
                        .orderByAsc(HomeworkAttachmentPo::getSortNo)
                        .orderByAsc(HomeworkAttachmentPo::getId))
                .stream().collect(Collectors.groupingBy(HomeworkAttachmentPo::getHomeworkId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, HomeworkSubmissionPo> loadLatestSubmissionMap(List<HomeworkTaskPo> tasks) {
        Set<Long> submissionIds = defaultList(tasks).stream().map(HomeworkTaskPo::getLatestSubmissionId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        if (submissionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return homeworkSubmissionMapper.selectList(new LambdaQueryWrapper<HomeworkSubmissionPo>().in(HomeworkSubmissionPo::getId, submissionIds))
                .stream().collect(Collectors.toMap(HomeworkSubmissionPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, List<HomeworkSubmissionAssetPo>> loadSubmissionAssetMap(Set<Long> submissionIds) {
        if (submissionIds == null || submissionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return homeworkSubmissionAssetMapper.selectList(new LambdaQueryWrapper<HomeworkSubmissionAssetPo>()
                        .in(HomeworkSubmissionAssetPo::getSubmissionId, submissionIds)
                        .orderByAsc(HomeworkSubmissionAssetPo::getSortNo)
                        .orderByAsc(HomeworkSubmissionAssetPo::getId))
                .stream().collect(Collectors.groupingBy(HomeworkSubmissionAssetPo::getSubmissionId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, HomeworkReviewPo> loadLatestReviewMap(List<HomeworkTaskPo> tasks) {
        Set<Long> taskIds = defaultList(tasks).stream().map(HomeworkTaskPo::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        if (taskIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<HomeworkReviewPo> reviews = homeworkReviewMapper.selectList(new LambdaQueryWrapper<HomeworkReviewPo>()
                .in(HomeworkReviewPo::getTaskId, taskIds)
                .orderByDesc(HomeworkReviewPo::getReviewedAt)
                .orderByDesc(HomeworkReviewPo::getId));
        Map<Long, HomeworkReviewPo> reviewMap = new LinkedHashMap<>();
        for (HomeworkReviewPo review : reviews) {
            reviewMap.putIfAbsent(review.getTaskId(), review);
        }
        return reviewMap;
    }

    private Map<Long, String> loadChildNameMap(List<NotificationPo> notifications, Map<Long, StudentPo> studentMap, Map<Long, UserPo> studentUserMap) {
        if (notifications.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        Set<Long> reviewIds = notifications.stream().filter(item -> "review_result".equalsIgnoreCase(item.getBizType())).map(NotificationPo::getBizId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, HomeworkReviewPo> reviewMap = reviewIds.isEmpty() ? Collections.emptyMap() : homeworkReviewMapper.selectList(new LambdaQueryWrapper<HomeworkReviewPo>().in(HomeworkReviewPo::getId, reviewIds))
                .stream().collect(Collectors.toMap(HomeworkReviewPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Set<Long> homeworkIds = notifications.stream().filter(item -> !"review_result".equalsIgnoreCase(item.getBizType())).map(NotificationPo::getBizId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, HomeworkTaskPo> taskMap = homeworkIds.isEmpty() ? Collections.emptyMap() : homeworkTaskMapper.selectList(new LambdaQueryWrapper<HomeworkTaskPo>()
                        .in(HomeworkTaskPo::getHomeworkId, homeworkIds)
                        .in(HomeworkTaskPo::getStudentId, studentMap.keySet())
                        .eq(HomeworkTaskPo::getIsDeleted, false)
                        .orderByAsc(HomeworkTaskPo::getId))
                .stream().collect(Collectors.toMap(HomeworkTaskPo::getHomeworkId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        for (NotificationPo notification : notifications) {
            Long studentId = null;
            if ("review_result".equalsIgnoreCase(notification.getBizType())) {
                HomeworkReviewPo review = reviewMap.get(notification.getBizId());
                if (review != null) {
                    studentId = review.getStudentId();
                }
            } else {
                HomeworkTaskPo task = taskMap.get(notification.getBizId());
                if (task != null) {
                    studentId = task.getStudentId();
                }
            }
            if (studentId == null) {
                continue;
            }
            StudentPo student = studentMap.get(studentId);
            UserPo studentUser = student == null ? null : studentUserMap.get(student.getStudentUserId());
            if (studentUser != null) {
                result.put(notification.getId(), studentUser.getUserName());
            }
        }
        return result;
    }

    private Map<Long, TaskSummary> loadTaskSummaryMap(Set<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<HomeworkTaskPo> tasks = homeworkTaskMapper.selectList(new LambdaQueryWrapper<HomeworkTaskPo>()
                .in(HomeworkTaskPo::getStudentId, studentIds)
                .eq(HomeworkTaskPo::getIsDeleted, false));
        if (tasks.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> homeworkIds = tasks.stream().map(HomeworkTaskPo::getHomeworkId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, HomeworkPo> homeworkMap = homeworkMapper.selectList(new LambdaQueryWrapper<HomeworkPo>().in(HomeworkPo::getId, homeworkIds))
                .stream().filter(this::isVisibleHomework)
                .collect(Collectors.toMap(HomeworkPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, TaskSummary> summaryMap = new LinkedHashMap<>();
        for (HomeworkTaskPo task : tasks) {
            HomeworkPo homework = homeworkMap.get(task.getHomeworkId());
            if (homework == null) {
                continue;
            }
            String status = resolveStatus(task, homework);
            TaskSummary summary = summaryMap.getOrDefault(task.getStudentId(), new TaskSummary());
            summaryMap.put(task.getStudentId(), summary.plus(status));
        }
        return summaryMap;
    }

    private AttachmentVo buildAttachmentVo(HomeworkAttachmentPo po) {
        AttachmentVo vo = new AttachmentVo();
        vo.setId(String.valueOf(po.getId()));
        vo.setName(defaultString(po.getAssetName(), "\u9644\u4ef6"));
        vo.setType(defaultString(po.getAssetType(), "file"));
        vo.setUrl(defaultString(po.getAssetUrl(), ""));
        return vo;
    }

    private SubmissionVo buildSubmissionVo(HomeworkSubmissionPo submission, List<HomeworkSubmissionAssetPo> assets) {
        if (submission == null) {
            return null;
        }
        SubmissionVo vo = new SubmissionVo();
        vo.setId(String.valueOf(submission.getId()));
        vo.setOperatorRole(submission.getOperatorRole());
        vo.setText(defaultString(submission.getSubmitText(), ""));
        vo.setImages(defaultList(assets).stream().map(HomeworkSubmissionAssetPo::getAssetUrl).filter(StringUtils::hasText).map(String::trim).toList());
        vo.setSubmittedAt(formatTime(submission.getSubmittedAt()));
        vo.setAssistedByParent("parent".equalsIgnoreCase(submission.getOperatorRole()));
        vo.setVersionNo(submission.getVersionNo());
        return vo;
    }

    private ReviewVo buildReviewVo(HomeworkReviewPo review) {
        if (review == null) {
            return null;
        }
        ReviewVo vo = new ReviewVo();
        vo.setId(String.valueOf(review.getId()));
        vo.setStatus(defaultString(review.getReviewStatus(), "unreviewed"));
        vo.setScore(review.getScore());
        vo.setLevel(trimToNull(review.getScoreLevel()));
        vo.setComment(defaultString(review.getCommentText(), ""));
        vo.setReviewedAt(formatTime(review.getReviewedAt()));
        return vo;
    }

    private MessageVo buildMessageVo(NotificationPo notification, String childName) {
        MessageVo vo = new MessageVo();
        vo.setId(String.valueOf(notification.getId()));
        vo.setTitle(defaultString(notification.getNotifyTitle(), "\u901a\u77e5\u6d88\u606f"));
        vo.setContent(defaultString(notification.getNotifyContent(), ""));
        vo.setTime(formatTime(notification.getSentAt() == null ? notification.getCreatedAt() : notification.getSentAt()));
        vo.setKind(resolveMessageKind(notification.getBizType()));
        vo.setUnread(notification.getReadAt() == null);
        vo.setChildName(childName);
        return vo;
    }

    private String resolveStatus(HomeworkTaskPo task, HomeworkPo homework) {
        String taskStatus = defaultString(task.getTaskStatus(), "pending").toLowerCase(Locale.ROOT);
        if ("revision_required".equals(taskStatus)) {
            return "revision";
        }
        if ("submitted".equals(taskStatus)) {
            return "submitted";
        }
        if ("completed".equals(taskStatus)) {
            return "completed";
        }
        if ("overdue".equals(taskStatus)) {
            return "overdue";
        }
        if (homework.getDeadlineAt() != null && LocalDateTime.now().isAfter(homework.getDeadlineAt())) {
            return "overdue";
        }
        return "pending";
    }

    private boolean matchesTab(String status, String tab) {
        if (!StringUtils.hasText(tab) || "all".equalsIgnoreCase(tab)) {
            return true;
        }
        return normalizeTab(tab).equalsIgnoreCase(defaultString(status, "pending"));
    }

    private String normalizeTab(String tab) {
        if (!StringUtils.hasText(tab)) {
            return "all";
        }
        String normalized = tab.trim().toLowerCase(Locale.ROOT);
        return "revision_required".equals(normalized) ? "revision" : normalized;
    }

    private boolean isVisibleHomework(HomeworkPo homework) {
        String status = defaultString(homework.getStatus(), "draft").toLowerCase(Locale.ROOT);
        return "published".equals(status) || "closed".equals(status);
    }

    private boolean matchesReadStatus(NotificationPo notification, String readStatus) {
        if (!StringUtils.hasText(readStatus) || "all".equalsIgnoreCase(readStatus)) {
            return true;
        }
        if ("read".equalsIgnoreCase(readStatus)) {
            return notification.getReadAt() != null;
        }
        if ("unread".equalsIgnoreCase(readStatus)) {
            return notification.getReadAt() == null;
        }
        return true;
    }

    private String resolveMessageKind(String bizType) {
        String normalized = defaultString(bizType, "").toLowerCase(Locale.ROOT);
        if ("review_result".equals(normalized)) {
            return "review";
        }
        if ("deadline_reminder".equals(normalized) || "submission_reminder".equals(normalized)) {
            return "remind";
        }
        if ("custom_notice".equals(normalized) || "system_notice".equals(normalized)) {
            return "system";
        }
        return "assignment";
    }

    private String subjectName(String subjectCode) {
        if (!StringUtils.hasText(subjectCode)) {
            return "\u7efc\u5408";
        }
        return SUBJECT_NAME_MAP.getOrDefault(subjectCode.trim().toLowerCase(Locale.ROOT), subjectCode.trim());
    }

    private List<String> splitSubmitTypes(String submitTypeMask) {
        if (!StringUtils.hasText(submitTypeMask)) {
            return Collections.emptyList();
        }
        return List.of(submitTypeMask.split(",")).stream().map(String::trim).filter(StringUtils::hasText).toList();
    }

    private String buildSummary(String content) {
        String normalized = defaultString(content, "").trim();
        if (!StringUtils.hasText(normalized)) {
            return "\u8bf7\u6309\u8981\u6c42\u5b8c\u6210\u5e76\u53ca\u65f6\u63d0\u4ea4\u3002";
        }
        return normalized.length() <= 36 ? normalized : normalized.substring(0, 36) + "...";
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : TIME_FORMATTER.format(time);
    }

    private String trimToNull(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private int defaultInteger(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private <T> List<T> defaultList(Collection<T> source) {
        return source == null ? Collections.emptyList() : new ArrayList<>(source);
    }

    private <T> PageSlice<T> slice(List<T> source, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int total = source == null ? 0 : source.size();
        if (total == 0) {
            return new PageSlice<>(Collections.emptyList(), 0, safePageNo, safePageSize);
        }
        int fromIndex = Math.min((safePageNo - 1) * safePageSize, total);
        int toIndex = Math.min(fromIndex + safePageSize, total);
        return new PageSlice<>(source.subList(fromIndex, toIndex), total, safePageNo, safePageSize);
    }

    private record PageSlice<T>(List<T> items, long total, int pageNo, int pageSize) {
    }

    private record TaskSummary(int pendingCount, int submittedCount, int revisionCount) {
        private TaskSummary() {
            this(0, 0, 0);
        }

        private TaskSummary plus(String status) {
            if ("revision".equals(status)) {
                return new TaskSummary(pendingCount, submittedCount, revisionCount + 1);
            }
            if ("submitted".equals(status) || "completed".equals(status)) {
                return new TaskSummary(pendingCount, submittedCount + 1, revisionCount);
            }
            return new TaskSummary(pendingCount + 1, submittedCount, revisionCount);
        }
    }
}


