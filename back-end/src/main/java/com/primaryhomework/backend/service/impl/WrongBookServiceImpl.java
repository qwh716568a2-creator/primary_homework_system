package com.primaryhomework.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.primaryhomework.backend.entity.dto.WrongBookAssetDto;
import com.primaryhomework.backend.entity.dto.WrongBookCreateDto;
import com.primaryhomework.backend.entity.dto.WrongBookFixDto;
import com.primaryhomework.backend.entity.dto.WrongBookQueryDto;
import com.primaryhomework.backend.entity.dto.WrongItemDto;
import com.primaryhomework.backend.entity.po.HomeworkPo;
import com.primaryhomework.backend.entity.po.HomeworkReviewPo;
import com.primaryhomework.backend.entity.po.HomeworkSubmissionPo;
import com.primaryhomework.backend.entity.po.HomeworkTaskPo;
import com.primaryhomework.backend.entity.po.ParentPo;
import com.primaryhomework.backend.entity.po.ParentStudentPo;
import com.primaryhomework.backend.entity.po.StudentPo;
import com.primaryhomework.backend.entity.po.TeacherClassSubjectPo;
import com.primaryhomework.backend.entity.po.TeacherPo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.po.WrongBookAssetPo;
import com.primaryhomework.backend.entity.po.WrongBookItemPo;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookAssetVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookDetailVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookListVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookSaveVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkAssetVo;
import com.primaryhomework.backend.entity.vo.teacher.ReviewWrongItemVo;
import com.primaryhomework.backend.mapper.HomeworkMapper;
import com.primaryhomework.backend.mapper.HomeworkReviewMapper;
import com.primaryhomework.backend.mapper.HomeworkSubmissionMapper;
import com.primaryhomework.backend.mapper.HomeworkTaskMapper;
import com.primaryhomework.backend.mapper.ParentMapper;
import com.primaryhomework.backend.mapper.ParentStudentMapper;
import com.primaryhomework.backend.mapper.StudentMapper;
import com.primaryhomework.backend.mapper.TeacherClassSubjectMapper;
import com.primaryhomework.backend.mapper.TeacherMapper;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.mapper.WrongBookAssetMapper;
import com.primaryhomework.backend.mapper.WrongBookItemMapper;
import com.primaryhomework.backend.service.WrongBookService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.CurrentUserSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongBookServiceImpl implements WrongBookService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, String> WRONG_REASON_LABEL_MAP = Map.ofEntries(
            Map.entry("calc_error", "计算错误"),
            Map.entry("concept_error", "概念不清"),
            Map.entry("reading_error", "审题错误"),
            Map.entry("writing_error", "书写问题"),
            Map.entry("careless_error", "粗心出错"),
            Map.entry("other", "其他")
    );
    private static final Map<String, String> SUBJECT_NAME_MAP = Map.ofEntries(
            Map.entry("math", "数学"),
            Map.entry("chinese", "语文"),
            Map.entry("english", "英语"),
            Map.entry("science", "科学"),
            Map.entry("moral", "道德与法治"),
            Map.entry("morality", "道德与法治"),
            Map.entry("art", "美术"),
            Map.entry("music", "音乐"),
            Map.entry("pe", "体育")
    );

    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final ParentMapper parentMapper;
    private final ParentStudentMapper parentStudentMapper;
    private final TeacherClassSubjectMapper teacherClassSubjectMapper;
    private final HomeworkMapper homeworkMapper;
    private final HomeworkTaskMapper homeworkTaskMapper;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final HomeworkReviewMapper homeworkReviewMapper;
    private final WrongBookItemMapper wrongBookItemMapper;
    private final WrongBookAssetMapper wrongBookAssetMapper;

    @Override
    @Transactional
    public int saveTeacherWrongItems(UserPo teacherUser,
                                     HomeworkPo homework,
                                     HomeworkTaskPo task,
                                     HomeworkSubmissionPo submission,
                                     HomeworkReviewPo review,
                                     List<WrongItemDto> wrongItems) {
        if (review == null || wrongItems == null || wrongItems.isEmpty()) {
            return 0;
        }

        StudentPo student = requireStudentById(task.getStudentId());
        validateTeacherScope(teacherUser, task, homework);

        Set<String> existingKeys = wrongBookItemMapper.selectList(
                        new LambdaQueryWrapper<WrongBookItemPo>()
                                .eq(WrongBookItemPo::getReviewId, review.getId())
                                .eq(WrongBookItemPo::getSourceType, "teacher_mark")
                ).stream()
                .map(item -> normalizeQuestionKey(item.getQuestionNo()))
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> currentKeys = new LinkedHashSet<>();
        int insertedCount = 0;
        for (WrongItemDto wrongItem : wrongItems) {
            if (wrongItem == null || isEmptyWrongItem(wrongItem)) {
                continue;
            }
            String questionKey = normalizeQuestionKey(wrongItem.getQuestionNo());
            if (StringUtils.hasText(questionKey) && (existingKeys.contains(questionKey) || !currentKeys.add(questionKey))) {
                continue;
            }

            WrongBookItemPo itemPo = new WrongBookItemPo();
            itemPo.setSchoolId(student.getSchoolId());
            itemPo.setStudentId(student.getId());
            itemPo.setHomeworkId(task.getHomeworkId());
            itemPo.setTaskId(task.getId());
            itemPo.setSubmissionId(submission.getId());
            itemPo.setReviewId(review.getId());
            itemPo.setSubjectCode(canonicalSubjectCode(homework.getSubjectCode()));
            itemPo.setSourceType("teacher_mark");
            itemPo.setQuestionNo(trimToNull(wrongItem.getQuestionNo()));
            itemPo.setQuestionText(trimToNull(wrongItem.getQuestionText()));
            itemPo.setStudentAnswer(trimToNull(wrongItem.getStudentAnswer()));
            itemPo.setCorrectAnswer(trimToNull(wrongItem.getCorrectAnswer()));
            itemPo.setAnalysisText(trimToNull(wrongItem.getAnalysisText()));
            itemPo.setWrongReasonCode(trimToNull(wrongItem.getWrongReasonCode()));
            itemPo.setStatus("pending_fix");
            itemPo.setAddedByUserId(teacherUser.getId());
            itemPo.setAddedByRole("teacher");
            itemPo.setRecognizedConfidence(null);
            itemPo.setFixCount(0);
            wrongBookItemMapper.insert(itemPo);
            saveAssets(itemPo.getId(), wrongItem.getAssets(), "question_image");
            insertedCount++;
        }
        return insertedCount;
    }

    @Override
    public Map<Long, List<ReviewWrongItemVo>> loadReviewWrongItemMap(Set<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<WrongBookItemPo> items = wrongBookItemMapper.selectList(
                new LambdaQueryWrapper<WrongBookItemPo>()
                        .in(WrongBookItemPo::getReviewId, reviewIds)
                        .eq(WrongBookItemPo::getSourceType, "teacher_mark")
                        .orderByAsc(WrongBookItemPo::getId)
        );
        if (items.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<WrongBookAssetPo>> assetMap = loadAssetMap(items.stream()
                .map(WrongBookItemPo::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return items.stream().collect(Collectors.groupingBy(
                WrongBookItemPo::getReviewId,
                LinkedHashMap::new,
                Collectors.mapping(item -> toReviewWrongItemVo(item, assetMap.get(item.getId())), Collectors.toList())
        ));
    }

    @Override
    public PageDTO<WrongBookListVo> pageStudentWrongBooks(String authorization, WrongBookQueryDto queryDto) {
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        return pageWrongBooks(student.getId(), queryDto);
    }

    @Override
    public WrongBookDetailVo getStudentWrongBook(String authorization, Long wrongBookId) {
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        WrongBookItemPo itemPo = requireWrongBookForStudent(student.getId(), wrongBookId);
        return toDetailVo(itemPo, loadAssetMap(Set.of(itemPo.getId())).get(itemPo.getId()));
    }

    @Override
    @Transactional
    public WrongBookSaveVo createStudentWrongBook(String authorization, WrongBookCreateDto createDto) {
        if (createDto == null) {
            throw new CommonException("请求体不能为空");
        }
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());

        HomeworkTaskPo task = null;
        if (createDto.getTaskId() != null) {
            task = requireTask(student.getId(), createDto.getTaskId());
        }
        HomeworkReviewPo review = null;
        if (createDto.getReviewId() != null) {
            review = requireReview(student.getId(), createDto.getReviewId());
            if (task == null && review.getTaskId() != null) {
                task = requireTask(student.getId(), review.getTaskId());
            }
        }
        HomeworkSubmissionPo submission = null;
        Long submissionId = createDto.getSubmissionId();
        if (submissionId == null && review != null) {
            submissionId = review.getSubmissionId();
        }
        if (submissionId != null) {
            submission = requireSubmission(student.getId(), submissionId);
        }
        HomeworkPo homework = null;
        Long homeworkId = createDto.getHomeworkId();
        if (homeworkId == null && review != null) {
            homeworkId = review.getHomeworkId();
        }
        if (homeworkId == null && task != null) {
            homeworkId = task.getHomeworkId();
        }
        if (homeworkId != null) {
            homework = homeworkMapper.selectById(homeworkId);
            if (homework == null) {
                throw new CommonException("作业不存在");
            }
        }

        String subjectCode = canonicalSubjectCode(resolveCreateSubjectCode(createDto, homework, task, review));
        if (!StringUtils.hasText(subjectCode)) {
            throw new CommonException("subjectCode不能为空");
        }
        String questionKey = normalizeQuestionKey(createDto.getQuestionNo());
        if (review != null && StringUtils.hasText(questionKey)) {
            long count = wrongBookItemMapper.selectCount(
                    new LambdaQueryWrapper<WrongBookItemPo>()
                            .eq(WrongBookItemPo::getReviewId, review.getId())
                            .eq(WrongBookItemPo::getQuestionNo, questionKey)
                            .eq(WrongBookItemPo::getSourceType, "student_manual")
            );
            if (count > 0) {
                throw new CommonException("该题已存在于错题本中");
            }
        }

        WrongBookItemPo itemPo = new WrongBookItemPo();
        itemPo.setSchoolId(student.getSchoolId());
        itemPo.setStudentId(student.getId());
        itemPo.setHomeworkId(homeworkId);
        itemPo.setTaskId(task == null ? null : task.getId());
        itemPo.setSubmissionId(submission == null ? null : submission.getId());
        itemPo.setReviewId(review == null ? null : review.getId());
        itemPo.setSubjectCode(subjectCode);
        itemPo.setSourceType("student_manual");
        itemPo.setQuestionNo(trimToNull(createDto.getQuestionNo()));
        itemPo.setQuestionText(trimToNull(createDto.getQuestionText()));
        itemPo.setStudentAnswer(trimToNull(createDto.getStudentAnswer()));
        itemPo.setCorrectAnswer(trimToNull(createDto.getCorrectAnswer()));
        itemPo.setAnalysisText(trimToNull(createDto.getAnalysisText()));
        itemPo.setWrongReasonCode(trimToNull(createDto.getWrongReasonCode()));
        itemPo.setStatus("pending_fix");
        itemPo.setAddedByUserId(studentUser.getId());
        itemPo.setAddedByRole("student");
        itemPo.setFixCount(0);
        wrongBookItemMapper.insert(itemPo);
        saveAssets(itemPo.getId(), createDto.getAssets(), "question_image");

        WrongBookSaveVo vo = new WrongBookSaveVo();
        vo.setId(itemPo.getId());
        vo.setWrongBookId(itemPo.getId());
        return vo;
    }

    @Override
    @Transactional
    public void fixStudentWrongBook(String authorization, Long wrongBookId, WrongBookFixDto fixDto) {
        if (fixDto == null) {
            throw new CommonException("请求体不能为空");
        }
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        WrongBookItemPo itemPo = requireWrongBookForStudent(student.getId(), wrongBookId);
        itemPo.setLastFixedText(trimToNull(fixDto.getFixedText()));
        itemPo.setLastFixedAt(LocalDateTime.now());
        itemPo.setFixCount(defaultInteger(itemPo.getFixCount(), 0) + 1);
        itemPo.setStatus("fixed");
        wrongBookItemMapper.updateById(itemPo);
        saveAssets(itemPo.getId(), fixDto.getAssets(), "correction_image");
    }

    @Override
    @Transactional
    public void markStudentWrongBookMastered(String authorization, Long wrongBookId) {
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        WrongBookItemPo itemPo = requireWrongBookForStudent(student.getId(), wrongBookId);
        itemPo.setStatus("mastered");
        wrongBookItemMapper.updateById(itemPo);
    }

    @Override
    public PageDTO<WrongBookListVo> pageParentWrongBooks(String authorization, Long studentId, WrongBookQueryDto queryDto) {
        UserPo parentUser = resolveParentUser(authorization);
        StudentPo student = requireBoundStudent(parentUser.getId(), studentId);
        return pageWrongBooks(student.getId(), queryDto);
    }

    private PageDTO<WrongBookListVo> pageWrongBooks(Long studentId, WrongBookQueryDto queryDto) {
        WrongBookQueryDto query = queryDto == null ? new WrongBookQueryDto() : queryDto;
        LambdaQueryWrapper<WrongBookItemPo> wrapper = new LambdaQueryWrapper<WrongBookItemPo>()
                .eq(WrongBookItemPo::getStudentId, studentId)
                .orderByDesc(WrongBookItemPo::getCreatedAt)
                .orderByDesc(WrongBookItemPo::getId);
        if (hasEffectiveFilter(query.getSubjectCode())) {
            wrapper.eq(WrongBookItemPo::getSubjectCode, canonicalSubjectCode(query.getSubjectCode()));
        }
        if (hasEffectiveFilter(query.getStatus())) {
            wrapper.eq(WrongBookItemPo::getStatus, query.getStatus().trim());
        }

        List<WrongBookItemPo> all = wrongBookItemMapper.selectList(wrapper);
        PageSlice<WrongBookItemPo> slice = slice(all, query.getPageNo(), query.getPageSize());
        return PageDTO.of(
                slice.items().stream().map(this::toListVo).toList(),
                slice.total(),
                slice.pageNo(),
                slice.pageSize()
        );
    }

    private ReviewWrongItemVo toReviewWrongItemVo(WrongBookItemPo itemPo, List<WrongBookAssetPo> assets) {
        ReviewWrongItemVo vo = new ReviewWrongItemVo();
        vo.setQuestionNo(itemPo.getQuestionNo());
        vo.setQuestionText(itemPo.getQuestionText());
        vo.setStudentAnswer(itemPo.getStudentAnswer());
        vo.setCorrectAnswer(itemPo.getCorrectAnswer());
        vo.setAnalysisText(itemPo.getAnalysisText());
        vo.setWrongReasonCode(itemPo.getWrongReasonCode());
        vo.setAssets(defaultList(assets).stream().map(this::toHomeworkAssetVo).toList());
        return vo;
    }

    private WrongBookListVo toListVo(WrongBookItemPo itemPo) {
        WrongBookListVo vo = new WrongBookListVo();
        vo.setId(itemPo.getId());
        vo.setWrongBookId(itemPo.getId());
        vo.setHomeworkId(itemPo.getHomeworkId());
        vo.setTaskId(itemPo.getTaskId());
        vo.setReviewId(itemPo.getReviewId());
        vo.setSubjectCode(itemPo.getSubjectCode());
        vo.setSubjectName(subjectName(itemPo.getSubjectCode()));
        vo.setSourceType(itemPo.getSourceType());
        vo.setQuestionNo(itemPo.getQuestionNo());
        vo.setQuestionText(itemPo.getQuestionText());
        vo.setWrongReasonCode(itemPo.getWrongReasonCode());
        vo.setWrongReasonLabel(wrongReasonLabel(itemPo.getWrongReasonCode()));
        vo.setTeacherName(loadTeacherName(itemPo));
        vo.setStatus(itemPo.getStatus());
        vo.setCreatedAt(formatTime(itemPo.getCreatedAt()));
        vo.setLastFixedAt(formatTime(itemPo.getLastFixedAt()));
        return vo;
    }

    private WrongBookDetailVo toDetailVo(WrongBookItemPo itemPo, List<WrongBookAssetPo> assets) {
        WrongBookDetailVo vo = new WrongBookDetailVo();
        vo.setId(itemPo.getId());
        vo.setWrongBookId(itemPo.getId());
        vo.setHomeworkId(itemPo.getHomeworkId());
        vo.setTaskId(itemPo.getTaskId());
        vo.setReviewId(itemPo.getReviewId());
        vo.setSubjectCode(itemPo.getSubjectCode());
        vo.setSubjectName(subjectName(itemPo.getSubjectCode()));
        vo.setSourceType(itemPo.getSourceType());
        vo.setQuestionNo(itemPo.getQuestionNo());
        vo.setQuestionText(itemPo.getQuestionText());
        vo.setStudentAnswer(itemPo.getStudentAnswer());
        vo.setCorrectAnswer(itemPo.getCorrectAnswer());
        vo.setAnalysisText(itemPo.getAnalysisText());
        vo.setWrongReasonCode(itemPo.getWrongReasonCode());
        vo.setWrongReasonLabel(wrongReasonLabel(itemPo.getWrongReasonCode()));
        vo.setTeacherName(loadTeacherName(itemPo));
        vo.setStatus(itemPo.getStatus());
        vo.setLastFixedText(itemPo.getLastFixedText());
        vo.setLastFixedAt(formatTime(itemPo.getLastFixedAt()));
        vo.setFixCount(defaultInteger(itemPo.getFixCount(), 0));
        vo.setAssets(defaultList(assets).stream().map(this::toWrongBookAssetVo).toList());
        return vo;
    }

    private HomeworkAssetVo toHomeworkAssetVo(WrongBookAssetPo assetPo) {
        HomeworkAssetVo vo = new HomeworkAssetVo();
        vo.setAssetType(assetPo.getAssetType());
        vo.setAssetUrl(assetPo.getAssetUrl());
        vo.setAssetName(assetPo.getAssetName());
        return vo;
    }

    private WrongBookAssetVo toWrongBookAssetVo(WrongBookAssetPo assetPo) {
        WrongBookAssetVo vo = new WrongBookAssetVo();
        vo.setAssetRole(assetPo.getAssetRole());
        vo.setAssetType(assetPo.getAssetType());
        vo.setAssetUrl(assetPo.getAssetUrl());
        vo.setAssetName(assetPo.getAssetName());
        return vo;
    }

    private Map<Long, List<WrongBookAssetPo>> loadAssetMap(Set<Long> wrongBookIds) {
        if (wrongBookIds == null || wrongBookIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return wrongBookAssetMapper.selectList(
                        new LambdaQueryWrapper<WrongBookAssetPo>()
                                .in(WrongBookAssetPo::getWrongBookId, wrongBookIds)
                                .orderByAsc(WrongBookAssetPo::getSortNo)
                                .orderByAsc(WrongBookAssetPo::getId)
                ).stream()
                .collect(Collectors.groupingBy(WrongBookAssetPo::getWrongBookId, LinkedHashMap::new, Collectors.toList()));
    }

    private void saveAssets(Long wrongBookId, List<WrongBookAssetDto> assets, String defaultRole) {
        int sortNo = defaultInteger(loadAssetMap(Set.of(wrongBookId)).getOrDefault(wrongBookId, Collections.emptyList()).size(), 0) + 1;
        for (WrongBookAssetDto assetDto : defaultList(assets)) {
            if (assetDto == null || !StringUtils.hasText(assetDto.getAssetUrl())) {
                continue;
            }
            WrongBookAssetPo assetPo = new WrongBookAssetPo();
            assetPo.setWrongBookId(wrongBookId);
            assetPo.setAssetRole(defaultString(trimToNull(assetDto.getAssetRole()), defaultRole));
            assetPo.setAssetType(defaultString(trimToNull(assetDto.getAssetType()), "image"));
            assetPo.setAssetUrl(assetDto.getAssetUrl().trim());
            assetPo.setAssetName(trimToNull(assetDto.getAssetName()));
            assetPo.setSortNo(sortNo++);
            wrongBookAssetMapper.insert(assetPo);
        }
    }

    private void validateTeacherScope(UserPo teacherUser, HomeworkTaskPo task, HomeworkPo homework) {
        if (teacherUser == null) {
            throw new CommonException("教师身份无效");
        }
        long count = teacherClassSubjectMapper.selectCount(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .eq(TeacherClassSubjectPo::getTeacherId, teacherUser.getId())
                        .eq(TeacherClassSubjectPo::getClassId, task.getClassId())
                        .in(TeacherClassSubjectPo::getSubjectCode, subjectAliases(homework == null ? null : homework.getSubjectCode()))
                        .eq(TeacherClassSubjectPo::getStatus, "enabled")
        );
        if (count <= 0) {
            throw new CommonException("当前教师不能给该学生标记错题");
        }
    }

    private UserPo resolveStudentUser(String authorization) {
        return CurrentUserSupport.requireUser(authorization, "student", userMapper);
        /* if (parsedToken != null && "student".equalsIgnoreCase(parsedToken.roleType())) {
            UserPo user = userMapper.selectById(parsedToken.userId());
            if (isActiveUser(user, "student")) {
                return user;
            }
        }
        List<StudentPo> students = studentMapper.selectList(
                new LambdaQueryWrapper<StudentPo>()
                        .eq(StudentPo::getStatus, "enabled")
                        .orderByAsc(StudentPo::getId)
                        .last("limit 1")
        );
        for (StudentPo student : students) {
            UserPo user = userMapper.selectById(student.getStudentUserId());
            if (isActiveUser(user, "student")) {
                return user;
            }
        }
        throw new CommonException(40101, "请先以学生身份登录");
    }

        */
    }

    private UserPo resolveParentUser(String authorization) {
        return CurrentUserSupport.requireUser(authorization, "parent", userMapper);
        /* if (parsedToken != null && "parent".equalsIgnoreCase(parsedToken.roleType())) {
            UserPo user = userMapper.selectById(parsedToken.userId());
            if (isActiveUser(user, "parent")) {
                return user;
            }
        }
        List<ParentPo> parents = parentMapper.selectList(
                new LambdaQueryWrapper<ParentPo>()
                        .orderByAsc(ParentPo::getId)
                        .last("limit 1")
        );
        for (ParentPo parent : parents) {
            UserPo user = userMapper.selectById(parent.getParentUserId());
            if (isActiveUser(user, "parent")) {
                return user;
            }
        }
        throw new CommonException(40101, "请先以家长身份登录");
    }

        */
    }

    private boolean isActiveUser(UserPo user, String roleType) {
        return CurrentUserSupport.isActiveUser(user, roleType);
    }

    private StudentPo requireStudent(Long studentUserId) {
        StudentPo student = studentMapper.selectOne(
                new LambdaQueryWrapper<StudentPo>()
                        .eq(StudentPo::getStudentUserId, studentUserId)
                        .eq(StudentPo::getStatus, "enabled")
                        .last("limit 1")
        );
        if (student == null) {
            throw new CommonException("未找到学生档案");
        }
        return student;
    }

    private StudentPo requireStudentById(Long studentId) {
        StudentPo student = studentMapper.selectById(studentId);
        if (student == null || !"enabled".equalsIgnoreCase(defaultString(student.getStatus(), "enabled"))) {
            throw new CommonException("学生不存在");
        }
        return student;
    }

    private StudentPo requireBoundStudent(Long parentUserId, Long studentId) {
        if (studentId == null) {
            throw new CommonException("studentId不能为空");
        }
        long count = parentStudentMapper.selectCount(
                new LambdaQueryWrapper<ParentStudentPo>()
                        .eq(ParentStudentPo::getParentUserId, parentUserId)
                        .eq(ParentStudentPo::getStudentId, studentId)
                        .eq(ParentStudentPo::getStatus, "enabled")
        );
        if (count <= 0) {
            throw new CommonException("当前家长无权查看该学生错题本");
        }
        return requireStudentById(studentId);
    }

    private HomeworkTaskPo requireTask(Long studentId, Long taskId) {
        HomeworkTaskPo task = homeworkTaskMapper.selectById(taskId);
        if (task == null || Boolean.TRUE.equals(task.getIsDeleted()) || !Objects.equals(task.getStudentId(), studentId)) {
            throw new CommonException("作业任务不存在");
        }
        return task;
    }

    private HomeworkSubmissionPo requireSubmission(Long studentId, Long submissionId) {
        HomeworkSubmissionPo submission = homeworkSubmissionMapper.selectById(submissionId);
        if (submission == null || !Objects.equals(submission.getStudentId(), studentId)) {
            throw new CommonException("提交记录不存在");
        }
        return submission;
    }

    private HomeworkReviewPo requireReview(Long studentId, Long reviewId) {
        HomeworkReviewPo review = homeworkReviewMapper.selectById(reviewId);
        if (review == null || !Objects.equals(review.getStudentId(), studentId)) {
            throw new CommonException("批改记录不存在");
        }
        return review;
    }

    private WrongBookItemPo requireWrongBookForStudent(Long studentId, Long wrongBookId) {
        WrongBookItemPo itemPo = wrongBookItemMapper.selectById(wrongBookId);
        if (itemPo == null || !Objects.equals(itemPo.getStudentId(), studentId)) {
            throw new CommonException("错题不存在");
        }
        return itemPo;
    }

    private boolean isEmptyWrongItem(WrongItemDto wrongItem) {
        return !StringUtils.hasText(wrongItem.getQuestionNo())
                && !StringUtils.hasText(wrongItem.getQuestionText())
                && !StringUtils.hasText(wrongItem.getStudentAnswer())
                && !StringUtils.hasText(wrongItem.getCorrectAnswer())
                && !StringUtils.hasText(wrongItem.getAnalysisText());
    }

    private Set<String> subjectAliases(String subjectCode) {
        String normalized = canonicalSubjectCode(subjectCode);
        if (!StringUtils.hasText(normalized)) {
            return Collections.emptySet();
        }
        if ("morality".equals(normalized)) {
            return Set.of("morality", "moral");
        }
        return Set.of(normalized);
    }

    private String canonicalSubjectCode(String subjectCode) {
        if (!StringUtils.hasText(subjectCode)) {
            return null;
        }
        String normalized = subjectCode.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "moral" -> "morality";
            case "数学" -> "math";
            case "语文" -> "chinese";
            case "英语" -> "english";
            case "科学" -> "science";
            case "道德与法治", "道法" -> "morality";
            case "美术" -> "art";
            case "音乐" -> "music";
            case "体育" -> "pe";
            default -> normalized;
        };
    }

    private String subjectName(String subjectCode) {
        String normalized = canonicalSubjectCode(subjectCode);
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        return SUBJECT_NAME_MAP.getOrDefault(normalized, normalized);
    }

    private String normalizeQuestionKey(String questionNo) {
        return trimToNull(questionNo);
    }

    private String resolveCreateSubjectCode(WrongBookCreateDto createDto,
                                            HomeworkPo homework,
                                            HomeworkTaskPo task,
                                            HomeworkReviewPo review) {
        String subjectCode = canonicalSubjectCode(createDto.getSubjectCode());
        if (StringUtils.hasText(subjectCode)) {
            return subjectCode;
        }
        String subjectNameCode = canonicalSubjectCode(createDto.getSubjectName());
        if (StringUtils.hasText(subjectNameCode)) {
            return subjectNameCode;
        }
        if (homework != null && StringUtils.hasText(homework.getSubjectCode())) {
            return homework.getSubjectCode();
        }
        if (task != null && task.getHomeworkId() != null) {
            HomeworkPo taskHomework = homeworkMapper.selectById(task.getHomeworkId());
            if (taskHomework != null && StringUtils.hasText(taskHomework.getSubjectCode())) {
                return taskHomework.getSubjectCode();
            }
        }
        return null;
    }

    private boolean hasEffectiveFilter(String value) {
        return StringUtils.hasText(value) && !"all".equalsIgnoreCase(value.trim());
    }

    private String wrongReasonLabel(String wrongReasonCode) {
        String normalized = trimToNull(wrongReasonCode);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        return WRONG_REASON_LABEL_MAP.getOrDefault(normalized, normalized);
    }

    private String loadTeacherName(WrongBookItemPo itemPo) {
        if (itemPo == null || !"teacher_mark".equals(itemPo.getSourceType()) || itemPo.getAddedByUserId() == null) {
            return null;
        }
        UserPo teacherUser = userMapper.selectById(itemPo.getAddedByUserId());
        return teacherUser == null ? null : teacherUser.getUserName();
    }

    private String trimToNull(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private int defaultInteger(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : TIME_FORMATTER.format(time);
    }

    private <T> List<T> defaultList(Collection<T> items) {
        return items == null ? Collections.emptyList() : new ArrayList<>(items);
    }

    private <T> PageSlice<T> slice(List<T> source, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
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
}
