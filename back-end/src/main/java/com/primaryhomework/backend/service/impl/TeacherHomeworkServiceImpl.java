package com.primaryhomework.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.primaryhomework.backend.entity.dto.teacher.ClassBindDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkAssetDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkPageQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkRemindDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkReviewAssetDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkReviewDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkRevokeDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkSaveDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkStatsQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkTaskQueryDto;
import com.primaryhomework.backend.entity.po.HomeworkAttachmentPo;
import com.primaryhomework.backend.entity.po.HomeworkClassPo;
import com.primaryhomework.backend.entity.po.HomeworkPo;
import com.primaryhomework.backend.entity.po.HomeworkReviewAssetPo;
import com.primaryhomework.backend.entity.po.HomeworkReviewPo;
import com.primaryhomework.backend.entity.po.HomeworkSubmissionAssetPo;
import com.primaryhomework.backend.entity.po.HomeworkSubmissionPo;
import com.primaryhomework.backend.entity.po.HomeworkTaskPo;
import com.primaryhomework.backend.entity.po.OperationLogPo;
import com.primaryhomework.backend.entity.po.SchoolClassPo;
import com.primaryhomework.backend.entity.po.SchoolGradePo;
import com.primaryhomework.backend.entity.po.SchoolPo;
import com.primaryhomework.backend.entity.po.StudentPo;
import com.primaryhomework.backend.entity.po.TeacherClassSubjectPo;
import com.primaryhomework.backend.entity.po.TeacherPo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.teacher.ClassCandidateVo;
import com.primaryhomework.backend.entity.vo.teacher.ClassSubjectVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkAssetVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkDetailVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkListItemVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkOverviewStatsVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkPrintVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkSavedVo;
import com.primaryhomework.backend.entity.vo.teacher.ReviewSaveVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkTaskDetailVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkTaskListItemVo;
import com.primaryhomework.backend.entity.vo.teacher.TeachingClassVo;
import com.primaryhomework.backend.mapper.HomeworkAttachmentMapper;
import com.primaryhomework.backend.mapper.HomeworkClassMapper;
import com.primaryhomework.backend.mapper.HomeworkMapper;
import com.primaryhomework.backend.mapper.HomeworkReviewAssetMapper;
import com.primaryhomework.backend.mapper.HomeworkReviewMapper;
import com.primaryhomework.backend.mapper.HomeworkSubmissionAssetMapper;
import com.primaryhomework.backend.mapper.HomeworkSubmissionMapper;
import com.primaryhomework.backend.mapper.HomeworkTaskMapper;
import com.primaryhomework.backend.mapper.OperationLogMapper;
import com.primaryhomework.backend.mapper.SchoolClassMapper;
import com.primaryhomework.backend.mapper.SchoolGradeMapper;
import com.primaryhomework.backend.mapper.SchoolMapper;
import com.primaryhomework.backend.mapper.StudentMapper;
import com.primaryhomework.backend.mapper.TeacherClassSubjectMapper;
import com.primaryhomework.backend.mapper.TeacherMapper;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.service.TeacherHomeworkService;
import com.primaryhomework.backend.service.WrongBookService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.CurrentUserSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
public class TeacherHomeworkServiceImpl implements TeacherHomeworkService {

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
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final SchoolClassMapper schoolClassMapper;
    private final SchoolGradeMapper schoolGradeMapper;
    private final SchoolMapper schoolMapper;
    private final TeacherClassSubjectMapper teacherClassSubjectMapper;
    private final HomeworkMapper homeworkMapper;
    private final HomeworkAttachmentMapper homeworkAttachmentMapper;
    private final HomeworkClassMapper homeworkClassMapper;
    private final HomeworkTaskMapper homeworkTaskMapper;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final HomeworkSubmissionAssetMapper homeworkSubmissionAssetMapper;
    private final HomeworkReviewMapper homeworkReviewMapper;
    private final HomeworkReviewAssetMapper homeworkReviewAssetMapper;
    private final OperationLogMapper operationLogMapper;
    private final WrongBookService wrongBookService;

    @Override
    public List<TeachingClassVo> listTeachingClasses(String authorization, String subjectCode) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        List<TeacherClassSubjectPo> relations = listTeacherClassRelations(teacherUser.getId(), subjectCode);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> classIds = relations.stream()
                .map(TeacherClassSubjectPo::getClassId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SchoolClassPo> classMap = loadClassMap(classIds);
        Map<Long, SchoolGradePo> gradeMap = loadGradeMap(classMap.values().stream()
                .map(SchoolClassPo::getGradeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, SchoolPo> schoolMap = loadSchoolMap(classMap.values().stream()
                .map(SchoolClassPo::getSchoolId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, Integer> studentCountMap = loadStudentCountMap(classIds);

        return relations.stream()
                .map(item -> buildTeachingClassVo(
                        item,
                        classMap.get(item.getClassId()),
                        gradeMap,
                        schoolMap,
                        studentCountMap,
                        teacherUser.getId()
                ))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((TeachingClassVo item) -> defaultString(item.getGradeName(), ""))
                        .thenComparing(item -> defaultString(item.getClassName(), ""))
                        .thenComparing(item -> defaultString(item.getSubjectCode(), "")))
                .toList();
    }

    @Override
    public List<TeachingClassVo> listAvailableClasses(String authorization, String subjectCode) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        String normalizedSubjectCode = normalizeSubjectCode(subjectCode);

        List<SchoolClassPo> classes = schoolClassMapper.selectList(
                new LambdaQueryWrapper<SchoolClassPo>()
                        .eq(SchoolClassPo::getSchoolId, teacherUser.getSchoolId())
                        .eq(SchoolClassPo::getStatus, "enabled")
                        .orderByAsc(SchoolClassPo::getGradeId)
                        .orderByAsc(SchoolClassPo::getClassName)
        );
        if (classes.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> classIds = classes.stream()
                .map(SchoolClassPo::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SchoolGradePo> gradeMap = loadGradeMap(classes.stream()
                .map(SchoolClassPo::getGradeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, SchoolPo> schoolMap = loadSchoolMap(classes.stream()
                .map(SchoolClassPo::getSchoolId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, Integer> studentCountMap = loadStudentCountMap(classIds);

        Set<Long> occupiedClassIds = teacherClassSubjectMapper.selectList(
                        new LambdaQueryWrapper<TeacherClassSubjectPo>()
                                .in(TeacherClassSubjectPo::getSubjectCode, subjectCodeAliases(normalizedSubjectCode))
                                .eq(TeacherClassSubjectPo::getStatus, "enabled")
                ).stream()
                .map(TeacherClassSubjectPo::getClassId)
                .collect(Collectors.toSet());

        return classes.stream()
                .filter(item -> !occupiedClassIds.contains(item.getId()))
                .map(item -> buildTeachingClassVo(
                        item,
                        normalizedSubjectCode,
                        teacherUser.getId(),
                        gradeMap,
                        schoolMap,
                        studentCountMap
                ))
                .toList();
    }

    @Override
    public List<ClassCandidateVo> listBindingCandidates(String authorization, String keyword) {
        UserPo teacherUser = resolveTeacherUser(authorization);

        List<SchoolClassPo> classes = schoolClassMapper.selectList(
                new LambdaQueryWrapper<SchoolClassPo>()
                        .eq(SchoolClassPo::getSchoolId, teacherUser.getSchoolId())
                        .eq(SchoolClassPo::getStatus, "enabled")
                        .orderByAsc(SchoolClassPo::getGradeId)
                        .orderByAsc(SchoolClassPo::getClassName)
        );
        if (classes.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> classIds = classes.stream()
                .map(SchoolClassPo::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SchoolGradePo> gradeMap = loadGradeMap(classes.stream()
                .map(SchoolClassPo::getGradeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, SchoolPo> schoolMap = loadSchoolMap(classes.stream()
                .map(SchoolClassPo::getSchoolId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, Integer> studentCountMap = loadStudentCountMap(classIds);

        List<TeacherClassSubjectPo> relations = teacherClassSubjectMapper.selectList(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .in(TeacherClassSubjectPo::getClassId, classIds)
                        .eq(TeacherClassSubjectPo::getStatus, "enabled")
                        .orderByAsc(TeacherClassSubjectPo::getClassId)
                        .orderByAsc(TeacherClassSubjectPo::getSubjectCode)
                        .orderByAsc(TeacherClassSubjectPo::getId)
        );
        Map<Long, List<TeacherClassSubjectPo>> relationMap = relations.stream()
                .collect(Collectors.groupingBy(
                        TeacherClassSubjectPo::getClassId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        Map<Long, UserPo> teacherUserMap = loadUserMap(relations.stream()
                .map(TeacherClassSubjectPo::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return classes.stream()
                .map(item -> buildClassCandidateVo(
                        item,
                        gradeMap,
                        schoolMap,
                        studentCountMap,
                        relationMap.get(item.getId()),
                        teacherUserMap
                ))
                .filter(item -> matchesCandidateKeyword(item, normalizedKeyword))
                .toList();
    }
    @Override
    @Transactional
    public void bindClass(String authorization, ClassBindDto bindDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        if (bindDto == null) {
            throw new CommonException("\u8bf7\u6c42\u4f53\u4e0d\u80fd\u4e3a\u7a7a");
        }

        Long classId = bindDto.getClassId();
        String subjectCode = normalizeSubjectCode(bindDto.getSubjectCode());
        SchoolClassPo classPo = requireSchoolClass(teacherUser, classId);
        Set<String> aliases = subjectCodeAliases(subjectCode);

        TeacherClassSubjectPo selfRelation = teacherClassSubjectMapper.selectOne(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .eq(TeacherClassSubjectPo::getTeacherId, teacherUser.getId())
                        .eq(TeacherClassSubjectPo::getClassId, classId)
                        .in(TeacherClassSubjectPo::getSubjectCode, aliases)
                        .last("limit 1")
        );

        TeacherClassSubjectPo occupiedRelation = teacherClassSubjectMapper.selectOne(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .eq(TeacherClassSubjectPo::getClassId, classId)
                        .in(TeacherClassSubjectPo::getSubjectCode, aliases)
                        .eq(TeacherClassSubjectPo::getStatus, "enabled")
                        .last("limit 1")
        );
        if (occupiedRelation != null && !Objects.equals(occupiedRelation.getTeacherId(), teacherUser.getId())) {
            throw new CommonException("\u8be5\u73ed\u7ea7\u7684\u8be5\u5b66\u79d1\u5df2\u7ed1\u5b9a\u5176\u4ed6\u6559\u5e08");
        }

        if (Boolean.TRUE.equals(bindDto.getIsHeadTeacher())
                && !Objects.equals(classPo.getHomeroomTeacherId(), teacherUser.getId())) {
            classPo.setHomeroomTeacherId(teacherUser.getId());
            schoolClassMapper.updateById(classPo);
        }

        if (selfRelation != null) {
            selfRelation.setSubjectCode(subjectCode);
            selfRelation.setStatus("enabled");
            selfRelation.setIsHeadTeacher(Objects.equals(classPo.getHomeroomTeacherId(), teacherUser.getId()));
            teacherClassSubjectMapper.updateById(selfRelation);
        } else {
            TeacherClassSubjectPo relation = new TeacherClassSubjectPo();
            relation.setTeacherId(teacherUser.getId());
            relation.setClassId(classId);
            relation.setSubjectCode(subjectCode);
            relation.setIsHeadTeacher(Objects.equals(classPo.getHomeroomTeacherId(), teacherUser.getId()));
            relation.setStatus("enabled");
            try {
                teacherClassSubjectMapper.insert(relation);
            } catch (DuplicateKeyException e) {
                throw new CommonException("\u8be5\u73ed\u7ea7\u7684\u8be5\u5b66\u79d1\u5df2\u7ed1\u5b9a\u5176\u4ed6\u6559\u5e08");
            }
        }

        if (classPo.getHomeroomTeacherId() != null) {
            syncHeadTeacherFlags(classId, classPo.getHomeroomTeacherId());
        }
    }
    @Override
    @Transactional
    public void unbindClass(String authorization, Long classId, String subjectCode) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        String normalizedSubjectCode = normalizeSubjectCode(subjectCode);
        if (classId == null) {
            throw new CommonException("classId\u4e0d\u80fd\u4e3a\u7a7a");
        }

        int deleted = teacherClassSubjectMapper.delete(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .eq(TeacherClassSubjectPo::getTeacherId, teacherUser.getId())
                        .eq(TeacherClassSubjectPo::getClassId, classId)
                        .in(TeacherClassSubjectPo::getSubjectCode, subjectCodeAliases(normalizedSubjectCode))
        );
        if (deleted <= 0) {
            throw new CommonException("\u672a\u627e\u5230\u8981\u89e3\u7ed1\u7684\u73ed\u7ea7\u5b66\u79d1\u5173\u7cfb");
        }

        SchoolClassPo classPo = schoolClassMapper.selectById(classId);
        if (classPo != null && classPo.getHomeroomTeacherId() != null) {
            syncHeadTeacherFlags(classId, classPo.getHomeroomTeacherId());
        }
    }
    @Override
    public PageDTO<HomeworkListItemVo> pageHomeworks(String authorization, HomeworkPageQueryDto queryDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkPageQueryDto query = queryDto == null ? new HomeworkPageQueryDto() : queryDto;

        LambdaQueryWrapper<HomeworkPo> wrapper = new LambdaQueryWrapper<HomeworkPo>()
                .eq(HomeworkPo::getCreatorTeacherId, teacherUser.getId())
                .orderByDesc(HomeworkPo::getUpdatedAt)
                .orderByDesc(HomeworkPo::getId);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(HomeworkPo::getTitle, query.getKeyword().trim());
        }
        if (StringUtils.hasText(query.getSubjectCode())) {
            wrapper.eq(HomeworkPo::getSubjectCode, query.getSubjectCode().trim());
        }
        if (StringUtils.hasText(query.getStatus()) && !"all".equalsIgnoreCase(query.getStatus().trim())) {
            wrapper.eq(HomeworkPo::getStatus, query.getStatus().trim());
        } else {
            wrapper.ne(HomeworkPo::getStatus, "deleted");
        }
        if (query.getClassId() != null) {
            List<Long> homeworkIds = homeworkClassMapper.selectList(
                            new LambdaQueryWrapper<HomeworkClassPo>().eq(HomeworkClassPo::getClassId, query.getClassId())
                    ).stream()
                    .map(HomeworkClassPo::getHomeworkId)
                    .distinct()
                    .toList();
            if (homeworkIds.isEmpty()) {
                return emptyPage(query.getPageNo(), query.getPageSize());
            }
            wrapper.in(HomeworkPo::getId, homeworkIds);
        }

        List<HomeworkPo> allHomeworks = homeworkMapper.selectList(wrapper);
        PageSlice<HomeworkPo> pageSlice = slice(allHomeworks, query.getPageNo(), query.getPageSize());
        if (pageSlice.items().isEmpty()) {
            return PageDTO.of(Collections.emptyList(), pageSlice.total(), pageSlice.pageNo(), pageSlice.pageSize());
        }

        Set<Long> homeworkIds = pageSlice.items().stream().map(HomeworkPo::getId).collect(Collectors.toSet());
        Map<Long, List<HomeworkClassPo>> homeworkClassMap = groupByHomeworkId(homeworkIds);
        Map<Long, SchoolClassPo> classMap = loadClassMap(
                homeworkClassMap.values().stream()
                        .flatMap(List::stream)
                        .map(HomeworkClassPo::getClassId)
                        .collect(Collectors.toSet())
        );
        Map<Long, List<HomeworkTaskPo>> taskMap = groupTasksByHomeworkId(homeworkIds);
        Map<Long, HomeworkPo> homeworkMap = pageSlice.items().stream()
                .collect(Collectors.toMap(HomeworkPo::getId, Function.identity()));

        List<HomeworkListItemVo> items = pageSlice.items().stream()
                .map(homework -> buildHomeworkListItem(
                        homework,
                        homeworkClassMap.get(homework.getId()),
                        classMap,
                        taskMap.get(homework.getId()),
                        homeworkMap
                ))
                .toList();

        return PageDTO.of(items, pageSlice.total(), pageSlice.pageNo(), pageSlice.pageSize());
    }

    @Override
    @Transactional
    public HomeworkSavedVo createHomework(String authorization, HomeworkSaveDto saveDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        validateHomeworkSave(teacherUser, saveDto);

        HomeworkPo homework = new HomeworkPo();
        homework.setSchoolId(teacherUser.getSchoolId());
        homework.setCreatorTeacherId(teacherUser.getId());
        homework.setSubjectCode(saveDto.getSubjectCode().trim());
        homework.setTitle(saveDto.getTitle().trim());
        homework.setContentText(trimToNull(saveDto.getContentText()));
        homework.setDeadlineAt(saveDto.getDeadlineAt());
        homework.setAllowLateSubmit(defaultBoolean(saveDto.getAllowLateSubmit(), false));
        homework.setAllowResubmit(defaultBoolean(saveDto.getAllowResubmit(), true));
        homework.setNeedParentConfirm(defaultBoolean(saveDto.getNeedParentConfirm(), false));
        homework.setSubmitTypeMask(joinSubmitTypes(saveDto.getSubmitTypes()));

        boolean publishNow = defaultBoolean(saveDto.getPublishNow(), false);
        homework.setStatus(publishNow ? "published" : "draft");
        homework.setPublishedAt(publishNow ? LocalDateTime.now() : null);
        homeworkMapper.insert(homework);

        replaceHomeworkClasses(homework.getId(), saveDto.getClassIds());
        replaceHomeworkAttachments(homework.getId(), saveDto.getAttachments());

        if (publishNow) {
            syncHomeworkTasks(homework);
        }

        HomeworkSavedVo vo = new HomeworkSavedVo();
        vo.setHomeworkId(homework.getId());
        vo.setStatus(homework.getStatus());
        return vo;
    }

    @Override
    @Transactional
    public void updateHomework(String authorization, Long homeworkId, HomeworkSaveDto saveDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        validateHomeworkSave(teacherUser, saveDto);

        HomeworkPo homework = requireOwnedHomework(teacherUser.getId(), homeworkId);
        if ("revoked".equalsIgnoreCase(homework.getStatus()) || "closed".equalsIgnoreCase(homework.getStatus())) {
            throw new CommonException("Current homework cannot be edited");
        }

        homework.setSubjectCode(saveDto.getSubjectCode().trim());
        homework.setTitle(saveDto.getTitle().trim());
        homework.setContentText(trimToNull(saveDto.getContentText()));
        homework.setDeadlineAt(saveDto.getDeadlineAt());
        homework.setAllowLateSubmit(defaultBoolean(saveDto.getAllowLateSubmit(), false));
        homework.setAllowResubmit(defaultBoolean(saveDto.getAllowResubmit(), true));
        homework.setNeedParentConfirm(defaultBoolean(saveDto.getNeedParentConfirm(), false));
        homework.setSubmitTypeMask(joinSubmitTypes(saveDto.getSubmitTypes()));
        homeworkMapper.updateById(homework);

        replaceHomeworkClasses(homework.getId(), saveDto.getClassIds());
        replaceHomeworkAttachments(homework.getId(), saveDto.getAttachments());

        if ("published".equalsIgnoreCase(homework.getStatus())) {
            syncHomeworkTasks(homework);
        }
    }

    @Override
    @Transactional
    public void publishHomework(String authorization, Long homeworkId) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkPo homework = requireOwnedHomework(teacherUser.getId(), homeworkId);
        if ("published".equalsIgnoreCase(homework.getStatus())) {
            syncHomeworkTasks(homework);
            return;
        }

        homework.setStatus("published");
        homework.setPublishedAt(LocalDateTime.now());
        homeworkMapper.updateById(homework);
        syncHomeworkTasks(homework);
    }

    @Override
    @Transactional
    public void revokeHomework(String authorization, Long homeworkId, HomeworkRevokeDto revokeDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkPo homework = requireOwnedHomework(teacherUser.getId(), homeworkId);
        if ("revoked".equalsIgnoreCase(homework.getStatus())) {
            return;
        }
        homework.setStatus("revoked");
        homeworkMapper.updateById(homework);
    }

    @Override
    @Transactional
    public void deleteHomework(String authorization, Long homeworkId) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkPo homework = requireOwnedHomework(teacherUser.getId(), homeworkId);

        Long submissionCount = homeworkSubmissionMapper.selectCount(
                new LambdaQueryWrapper<HomeworkSubmissionPo>()
                        .eq(HomeworkSubmissionPo::getHomeworkId, homeworkId)
        );
        if (submissionCount != null && submissionCount > 0 && "revoked".equalsIgnoreCase(homework.getStatus())) {
            homework.setStatus("deleted");
            homeworkMapper.updateById(homework);

            HomeworkTaskPo task = new HomeworkTaskPo();
            task.setIsDeleted(true);
            homeworkTaskMapper.update(
                    task,
                    new LambdaQueryWrapper<HomeworkTaskPo>()
                            .eq(HomeworkTaskPo::getHomeworkId, homeworkId)
            );
            return;
        }
        if (submissionCount != null && submissionCount > 0) {
            throw new CommonException("\u5df2\u6709\u5b66\u751f\u63d0\u4ea4\u8bb0\u5f55\uff0c\u4e0d\u80fd\u76f4\u63a5\u5220\u9664\uff0c\u8bf7\u5148\u64a4\u56de\u4f5c\u4e1a");
        }

        List<Long> reviewIds = homeworkReviewMapper.selectList(
                        new LambdaQueryWrapper<HomeworkReviewPo>()
                                .eq(HomeworkReviewPo::getHomeworkId, homeworkId)
                ).stream()
                .map(HomeworkReviewPo::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!reviewIds.isEmpty()) {
            homeworkReviewAssetMapper.delete(
                    new LambdaQueryWrapper<HomeworkReviewAssetPo>()
                            .in(HomeworkReviewAssetPo::getReviewId, reviewIds)
            );
        }
        homeworkReviewMapper.delete(
                new LambdaQueryWrapper<HomeworkReviewPo>()
                        .eq(HomeworkReviewPo::getHomeworkId, homeworkId)
        );

        List<Long> submissionIds = homeworkSubmissionMapper.selectList(
                        new LambdaQueryWrapper<HomeworkSubmissionPo>()
                                .eq(HomeworkSubmissionPo::getHomeworkId, homeworkId)
                ).stream()
                .map(HomeworkSubmissionPo::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!submissionIds.isEmpty()) {
            homeworkSubmissionAssetMapper.delete(
                    new LambdaQueryWrapper<HomeworkSubmissionAssetPo>()
                            .in(HomeworkSubmissionAssetPo::getSubmissionId, submissionIds)
            );
        }
        homeworkSubmissionMapper.delete(
                new LambdaQueryWrapper<HomeworkSubmissionPo>()
                        .eq(HomeworkSubmissionPo::getHomeworkId, homeworkId)
        );

        homeworkTaskMapper.delete(
                new LambdaQueryWrapper<HomeworkTaskPo>()
                        .eq(HomeworkTaskPo::getHomeworkId, homeworkId)
        );
        homeworkAttachmentMapper.delete(
                new LambdaQueryWrapper<HomeworkAttachmentPo>()
                        .eq(HomeworkAttachmentPo::getHomeworkId, homeworkId)
        );
        homeworkClassMapper.delete(
                new LambdaQueryWrapper<HomeworkClassPo>()
                        .eq(HomeworkClassPo::getHomeworkId, homeworkId)
        );
        homeworkMapper.deleteById(homeworkId);
    }

    @Override
    public HomeworkDetailVo getHomeworkDetail(String authorization, Long homeworkId) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkPo homework = requireOwnedHomework(teacherUser.getId(), homeworkId);

        List<HomeworkClassPo> homeworkClasses = homeworkClassMapper.selectList(
                new LambdaQueryWrapper<HomeworkClassPo>()
                        .eq(HomeworkClassPo::getHomeworkId, homeworkId)
                        .orderByAsc(HomeworkClassPo::getId)
        );
        Set<Long> classIds = homeworkClasses.stream().map(HomeworkClassPo::getClassId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SchoolClassPo> classMap = loadClassMap(classIds);
        Map<Long, Integer> studentCountMap = studentMapper.selectList(
                        classIds.isEmpty()
                                ? new LambdaQueryWrapper<StudentPo>().last("limit 0")
                                : new LambdaQueryWrapper<StudentPo>()
                                .in(StudentPo::getClassId, classIds)
                                .eq(StudentPo::getStatus, "enabled")
                ).stream()
                .collect(Collectors.groupingBy(
                        StudentPo::getClassId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        List<HomeworkTaskPo> tasks = homeworkTaskMapper.selectList(
                new LambdaQueryWrapper<HomeworkTaskPo>()
                        .eq(HomeworkTaskPo::getHomeworkId, homeworkId)
                        .eq(HomeworkTaskPo::getIsDeleted, false)
        );
        Map<Long, List<HomeworkTaskPo>> taskClassMap = tasks.stream().collect(Collectors.groupingBy(HomeworkTaskPo::getClassId));

        HomeworkDetailVo detailVo = new HomeworkDetailVo();
        HomeworkDetailVo.BaseInfo baseInfo = new HomeworkDetailVo.BaseInfo();
        baseInfo.setHomeworkId(homework.getId());
        baseInfo.setTitle(homework.getTitle());
        baseInfo.setSubjectCode(homework.getSubjectCode());
        baseInfo.setContentText(homework.getContentText());
        baseInfo.setDeadlineAt(homework.getDeadlineAt());
        baseInfo.setStatus(homework.getStatus());
        baseInfo.setAllowLateSubmit(defaultBoolean(homework.getAllowLateSubmit(), false));
        baseInfo.setAllowResubmit(defaultBoolean(homework.getAllowResubmit(), true));
        baseInfo.setNeedParentConfirm(defaultBoolean(homework.getNeedParentConfirm(), false));
        baseInfo.setSubmitTypes(splitSubmitTypes(homework.getSubmitTypeMask()));
        detailVo.setBaseInfo(baseInfo);

        List<HomeworkDetailVo.ClassSummary> classSummaries = new ArrayList<>();
        for (HomeworkClassPo homeworkClass : homeworkClasses) {
            SchoolClassPo classPo = classMap.get(homeworkClass.getClassId());
            if (classPo == null) {
                continue;
            }
            HomeworkDetailVo.ClassSummary summary = new HomeworkDetailVo.ClassSummary();
            summary.setClassId(classPo.getId());
            summary.setClassName(classPo.getClassName());
            summary.setStudentCount(studentCountMap.getOrDefault(classPo.getId(), 0));

            int submittedCount = 0;
            int completedCount = 0;
            int revisionRequiredCount = 0;
            int overdueCount = 0;

            for (HomeworkTaskPo task : taskClassMap.getOrDefault(classPo.getId(), Collections.emptyList())) {
                String taskStatus = resolveTaskStatus(task, homework);
                if (task.getLatestSubmissionId() != null) {
                    submittedCount++;
                }
                if ("completed".equals(taskStatus)) {
                    completedCount++;
                }
                if ("revision_required".equals(taskStatus)) {
                    revisionRequiredCount++;
                }
                if ("overdue".equals(taskStatus)) {
                    overdueCount++;
                }
            }

            summary.setSubmittedCount(submittedCount);
            summary.setCompletedCount(completedCount);
            summary.setRevisionRequiredCount(revisionRequiredCount);
            summary.setOverdueCount(overdueCount);
            classSummaries.add(summary);
        }
        detailVo.setClassList(classSummaries);

        detailVo.setAttachments(homeworkAttachmentMapper.selectList(
                        new LambdaQueryWrapper<HomeworkAttachmentPo>()
                                .eq(HomeworkAttachmentPo::getHomeworkId, homeworkId)
                                .orderByAsc(HomeworkAttachmentPo::getSortNo)
                                .orderByAsc(HomeworkAttachmentPo::getId)
                ).stream()
                .map(this::toAssetVo)
                .toList());

        return detailVo;
    }

    @Override
    public HomeworkPrintVo getHomeworkPrint(String authorization, Long homeworkId) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkPo homework = requireOwnedHomework(teacherUser.getId(), homeworkId);

        List<HomeworkClassPo> homeworkClasses = homeworkClassMapper.selectList(
                new LambdaQueryWrapper<HomeworkClassPo>()
                        .eq(HomeworkClassPo::getHomeworkId, homeworkId)
                        .orderByAsc(HomeworkClassPo::getId)
        );
        Set<Long> classIds = homeworkClasses.stream()
                .map(HomeworkClassPo::getClassId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SchoolClassPo> classMap = loadClassMap(classIds);

        List<StudentPo> students = classIds.isEmpty()
                ? Collections.emptyList()
                : studentMapper.selectList(
                        new LambdaQueryWrapper<StudentPo>()
                                .in(StudentPo::getClassId, classIds)
                                .eq(StudentPo::getStatus, "enabled")
                );
        Map<Long, UserPo> studentUserMap = loadUserMap(
                students.stream()
                        .map(StudentPo::getStudentUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );
        Map<Long, List<StudentPo>> studentClassMap = students.stream()
                .filter(item -> item.getClassId() != null)
                .collect(Collectors.groupingBy(StudentPo::getClassId, LinkedHashMap::new, Collectors.toList()));

        SchoolPo school = homework.getSchoolId() == null ? null : schoolMapper.selectById(homework.getSchoolId());

        HomeworkPrintVo vo = new HomeworkPrintVo();
        vo.setHomeworkId(homework.getId());
        vo.setTitle(homework.getTitle());
        vo.setPrintTitle(defaultString(homework.getTitle(), "作业打印"));
        vo.setTemplateType("homework");
        vo.setSchoolName(school == null ? null : school.getSchoolName());
        vo.setTeacherName(teacherUser.getUserName());
        vo.setSubjectCode(homework.getSubjectCode());
        vo.setSubjectName(subjectName(homework.getSubjectCode()));
        vo.setContentText(homework.getContentText());
        vo.setDeadlineAt(homework.getDeadlineAt());
        vo.setStatus(homework.getStatus());
        vo.setAllowLateSubmit(defaultBoolean(homework.getAllowLateSubmit(), false));
        vo.setAllowResubmit(defaultBoolean(homework.getAllowResubmit(), true));
        vo.setNeedParentConfirm(defaultBoolean(homework.getNeedParentConfirm(), false));
        vo.setSubmitTypes(splitSubmitTypes(homework.getSubmitTypeMask()));
        vo.setGeneratedAt(LocalDateTime.now());

        List<HomeworkPrintVo.ClassSummary> classList = new ArrayList<>();
        List<HomeworkPrintVo.ClassStudentGroup> studentGroups = new ArrayList<>();
        for (HomeworkClassPo homeworkClass : homeworkClasses) {
            SchoolClassPo classPo = classMap.get(homeworkClass.getClassId());
            if (classPo == null) {
                continue;
            }
            List<StudentPo> classStudents = new ArrayList<>(studentClassMap.getOrDefault(classPo.getId(), Collections.emptyList()));
            classStudents.sort(Comparator
                    .comparing((StudentPo item) -> defaultString(item.getStudentNo(), ""))
                    .thenComparing(item -> defaultString(resolveStudentName(item, studentUserMap), "")));

            HomeworkPrintVo.ClassSummary classSummary = new HomeworkPrintVo.ClassSummary();
            classSummary.setClassId(classPo.getId());
            classSummary.setClassName(classPo.getClassName());
            classSummary.setStudentCount(classStudents.size());
            classList.add(classSummary);

            HomeworkPrintVo.ClassStudentGroup group = new HomeworkPrintVo.ClassStudentGroup();
            group.setClassId(classPo.getId());
            group.setClassName(classPo.getClassName());
            group.setStudents(classStudents.stream()
                    .map(student -> toPrintStudentItem(student, studentUserMap))
                    .toList());
            studentGroups.add(group);
        }
        vo.setClassList(classList);
        vo.setStudentGroups(studentGroups);

        vo.setAttachments(homeworkAttachmentMapper.selectList(
                        new LambdaQueryWrapper<HomeworkAttachmentPo>()
                                .eq(HomeworkAttachmentPo::getHomeworkId, homeworkId)
                                .orderByAsc(HomeworkAttachmentPo::getSortNo)
                                .orderByAsc(HomeworkAttachmentPo::getId)
                ).stream()
                .map(this::toAssetVo)
                .toList());

        savePrintLog(teacherUser.getId(), homeworkId);
        return vo;
    }

    @Override
    public PageDTO<HomeworkTaskListItemVo> pageHomeworkTasks(String authorization, Long homeworkId, HomeworkTaskQueryDto queryDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkPo homework = requireOwnedHomework(teacherUser.getId(), homeworkId);
        HomeworkTaskQueryDto query = queryDto == null ? new HomeworkTaskQueryDto() : queryDto;

        LambdaQueryWrapper<HomeworkTaskPo> wrapper = new LambdaQueryWrapper<HomeworkTaskPo>()
                .eq(HomeworkTaskPo::getHomeworkId, homeworkId)
                .eq(HomeworkTaskPo::getIsDeleted, false);
        if (query.getClassId() != null) {
            wrapper.eq(HomeworkTaskPo::getClassId, query.getClassId());
        }

        List<HomeworkTaskPo> allTasks = homeworkTaskMapper.selectList(wrapper);
        if (allTasks.isEmpty()) {
            return emptyPage(query.getPageNo(), query.getPageSize());
        }

        Map<Long, StudentPo> studentMap = loadStudentMap(allTasks.stream().map(HomeworkTaskPo::getStudentId).collect(Collectors.toSet()));
        Map<Long, UserPo> studentUserMap = loadUserMap(
                studentMap.values().stream().map(StudentPo::getStudentUserId).collect(Collectors.toSet())
        );
        Map<Long, SchoolClassPo> classMap = loadClassMap(allTasks.stream().map(HomeworkTaskPo::getClassId).collect(Collectors.toSet()));

        List<HomeworkTaskListItemVo> items = allTasks.stream()
                .map(task -> toHomeworkTaskListItem(task, homework, studentMap, studentUserMap, classMap))
                .filter(Objects::nonNull)
                .filter(item -> !StringUtils.hasText(query.getTaskStatus()) || query.getTaskStatus().trim().equals(item.getTaskStatus()))
                .filter(item -> !StringUtils.hasText(query.getKeyword()) || item.getStudentName().contains(query.getKeyword().trim()))
                .sorted(Comparator
                        .comparing(HomeworkTaskListItemVo::getLatestSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(HomeworkTaskListItemVo::getTaskId))
                .toList();

        PageSlice<HomeworkTaskListItemVo> pageSlice = slice(items, query.getPageNo(), query.getPageSize());
        return PageDTO.of(pageSlice.items(), pageSlice.total(), pageSlice.pageNo(), pageSlice.pageSize());
    }

    @Override
    public HomeworkTaskDetailVo getTaskDetail(String authorization, Long taskId) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkTaskPo task = requireOwnedTask(teacherUser.getId(), taskId);
        HomeworkPo homework = homeworkMapper.selectById(task.getHomeworkId());

        Map<Long, StudentPo> studentMap = loadStudentMap(Set.of(task.getStudentId()));
        StudentPo student = studentMap.get(task.getStudentId());
        UserPo studentUser = student == null ? null : userMapper.selectById(student.getStudentUserId());

        HomeworkTaskDetailVo detailVo = new HomeworkTaskDetailVo();
        HomeworkTaskDetailVo.TaskInfo taskInfo = new HomeworkTaskDetailVo.TaskInfo();
        taskInfo.setTaskId(task.getId());
        taskInfo.setStudentId(task.getStudentId());
        taskInfo.setStudentName(studentUser == null ? "Student" : studentUser.getUserName());
        taskInfo.setTaskStatus(resolveTaskStatus(task, homework));
        taskInfo.setReviewStatus(defaultString(task.getLatestReviewStatus(), "unreviewed"));
        detailVo.setTaskInfo(taskInfo);

        List<HomeworkSubmissionPo> submissions = homeworkSubmissionMapper.selectList(
                new LambdaQueryWrapper<HomeworkSubmissionPo>()
                        .eq(HomeworkSubmissionPo::getTaskId, taskId)
                        .eq(HomeworkSubmissionPo::getSubmitStatus, "submitted")
                        .orderByAsc(HomeworkSubmissionPo::getVersionNo)
                        .orderByAsc(HomeworkSubmissionPo::getId)
        );
        Map<Long, List<HomeworkSubmissionAssetPo>> submissionAssetMap = submissions.isEmpty()
                ? Collections.emptyMap()
                : homeworkSubmissionAssetMapper.selectList(
                        new LambdaQueryWrapper<HomeworkSubmissionAssetPo>()
                                .in(HomeworkSubmissionAssetPo::getSubmissionId, submissions.stream().map(HomeworkSubmissionPo::getId).toList())
                                .orderByAsc(HomeworkSubmissionAssetPo::getSortNo)
                                .orderByAsc(HomeworkSubmissionAssetPo::getId)
                ).stream().collect(Collectors.groupingBy(HomeworkSubmissionAssetPo::getSubmissionId));

        detailVo.setSubmissions(submissions.stream().map(submission -> {
            HomeworkTaskDetailVo.Submission submissionVo = new HomeworkTaskDetailVo.Submission();
            submissionVo.setSubmissionId(submission.getId());
            submissionVo.setVersionNo(submission.getVersionNo());
            submissionVo.setOperatorRole(submission.getOperatorRole());
            submissionVo.setSubmittedAt(submission.getSubmittedAt());
            submissionVo.setSubmitText(submission.getSubmitText());
            submissionVo.setAssets(submissionAssetMap.getOrDefault(submission.getId(), Collections.emptyList())
                    .stream()
                    .map(this::toAssetVo)
                    .toList());
            return submissionVo;
        }).toList());

        List<HomeworkReviewPo> reviews = homeworkReviewMapper.selectList(
                new LambdaQueryWrapper<HomeworkReviewPo>()
                        .eq(HomeworkReviewPo::getTaskId, taskId)
                        .orderByAsc(HomeworkReviewPo::getReviewedAt)
                        .orderByAsc(HomeworkReviewPo::getId)
        );
        Map<Long, List<HomeworkReviewAssetPo>> reviewAssetMap = reviews.isEmpty()
                ? Collections.emptyMap()
                : homeworkReviewAssetMapper.selectList(
                        new LambdaQueryWrapper<HomeworkReviewAssetPo>()
                                .in(HomeworkReviewAssetPo::getReviewId, reviews.stream().map(HomeworkReviewPo::getId).toList())
                                .orderByAsc(HomeworkReviewAssetPo::getSortNo)
                                .orderByAsc(HomeworkReviewAssetPo::getId)
                ).stream().collect(Collectors.groupingBy(HomeworkReviewAssetPo::getReviewId));
        Map<Long, List<com.primaryhomework.backend.entity.vo.teacher.ReviewWrongItemVo>> wrongItemMap = reviews.isEmpty()
                ? Collections.emptyMap()
                : wrongBookService.loadReviewWrongItemMap(reviews.stream()
                .map(HomeworkReviewPo::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        detailVo.setReviews(reviews.stream().map(review -> {
            HomeworkTaskDetailVo.Review reviewVo = new HomeworkTaskDetailVo.Review();
            reviewVo.setReviewId(review.getId());
            reviewVo.setReviewStatus(review.getReviewStatus());
            reviewVo.setScore(review.getScore());
            reviewVo.setScoreLevel(review.getScoreLevel());
            reviewVo.setCommentText(review.getCommentText());
            reviewVo.setReviewedAt(review.getReviewedAt());
            reviewVo.setReviewAssets(reviewAssetMap.getOrDefault(review.getId(), Collections.emptyList())
                    .stream()
                    .map(this::toAssetVo)
                    .toList());
            reviewVo.setWrongItems(wrongItemMap.getOrDefault(review.getId(), Collections.emptyList()));
            return reviewVo;
        }).toList());

        return detailVo;
    }

    @Override
    @Transactional
    public ReviewSaveVo reviewTask(String authorization, Long taskId, HomeworkReviewDto reviewDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkTaskPo task = requireOwnedTask(teacherUser.getId(), taskId);
        HomeworkPo homework = requireOwnedHomework(teacherUser.getId(), task.getHomeworkId());
        HomeworkSubmissionPo submission = homeworkSubmissionMapper.selectById(reviewDto.getSubmissionId());
        if (submission == null || !Objects.equals(submission.getTaskId(), taskId)) {
            throw new CommonException("Submission does not belong to this task");
        }

        String reviewStatus = normalizeReviewStatus(reviewDto.getReviewStatus());
        LocalDateTime now = LocalDateTime.now();

        HomeworkReviewPo review = new HomeworkReviewPo();
        review.setTaskId(task.getId());
        review.setHomeworkId(task.getHomeworkId());
        review.setStudentId(task.getStudentId());
        review.setSubmissionId(submission.getId());
        review.setReviewerTeacherId(teacherUser.getId());
        review.setReviewStatus(reviewStatus);
        review.setScore(reviewDto.getScore());
        review.setScoreLevel(trimToNull(reviewDto.getScoreLevel()));
        review.setCommentText(trimToNull(reviewDto.getCommentText()));
        review.setReviewedAt(now);
        homeworkReviewMapper.insert(review);

        int sortNo = 1;
        for (HomeworkReviewAssetDto assetDto : defaultList(reviewDto.getReviewAssets())) {
            if (!StringUtils.hasText(assetDto.getAssetUrl())) {
                continue;
            }
            HomeworkReviewAssetPo assetPo = new HomeworkReviewAssetPo();
            assetPo.setReviewId(review.getId());
            assetPo.setAssetType(defaultString(assetDto.getAssetType(), "image"));
            assetPo.setAssetUrl(assetDto.getAssetUrl().trim());
            assetPo.setSortNo(sortNo++);
            homeworkReviewAssetMapper.insert(assetPo);
        }

        task.setLatestReviewStatus(reviewStatus);
        task.setLatestReviewedAt(now);
        task.setTaskStatus(reviewStatus);
        homeworkTaskMapper.updateById(task);

        int wrongItemCount = wrongBookService.saveTeacherWrongItems(
                teacherUser,
                homework,
                task,
                submission,
                review,
                reviewDto.getWrongItems()
        );

        ReviewSaveVo vo = new ReviewSaveVo();
        vo.setReviewId(review.getId());
        vo.setWrongItemCount(wrongItemCount);
        return vo;
    }

    @Override
    public void remindHomework(String authorization, Long homeworkId, HomeworkRemindDto remindDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        requireOwnedHomework(teacherUser.getId(), homeworkId);
        if (remindDto != null && remindDto.getClassId() != null) {
            boolean exists = homeworkClassMapper.selectCount(
                    new LambdaQueryWrapper<HomeworkClassPo>()
                            .eq(HomeworkClassPo::getHomeworkId, homeworkId)
                            .eq(HomeworkClassPo::getClassId, remindDto.getClassId())
            ) > 0;
            if (!exists) {
                throw new CommonException("Class is not assigned to current homework");
            }
        }
    }

    @Override
    public HomeworkOverviewStatsVo getOverview(String authorization, HomeworkStatsQueryDto queryDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        HomeworkStatsQueryDto query = queryDto == null ? new HomeworkStatsQueryDto() : queryDto;

        List<HomeworkPo> homeworks = homeworkMapper.selectList(
                new LambdaQueryWrapper<HomeworkPo>()
                        .eq(HomeworkPo::getCreatorTeacherId, teacherUser.getId())
                        .orderByDesc(HomeworkPo::getUpdatedAt)
        );

        if (StringUtils.hasText(query.getSubjectCode())) {
            homeworks = homeworks.stream()
                    .filter(item -> query.getSubjectCode().trim().equals(item.getSubjectCode()))
                    .toList();
        }
        if (query.getStartDate() != null || query.getEndDate() != null) {
            homeworks = homeworks.stream().filter(item -> {
                LocalDateTime compareTime = item.getPublishedAt() != null ? item.getPublishedAt() : item.getCreatedAt();
                if (compareTime == null) {
                    return true;
                }
                if (query.getStartDate() != null && compareTime.isBefore(query.getStartDate())) {
                    return false;
                }
                return query.getEndDate() == null || !compareTime.isAfter(query.getEndDate());
            }).toList();
        }
        if (query.getClassId() != null) {
            Set<Long> homeworkIds = homeworkClassMapper.selectList(
                            new LambdaQueryWrapper<HomeworkClassPo>().eq(HomeworkClassPo::getClassId, query.getClassId())
                    ).stream()
                    .map(HomeworkClassPo::getHomeworkId)
                    .collect(Collectors.toSet());
            homeworks = homeworks.stream().filter(item -> homeworkIds.contains(item.getId())).toList();
        }

        Set<Long> homeworkIds = homeworks.stream().map(HomeworkPo::getId).collect(Collectors.toSet());
        List<HomeworkTaskPo> tasks = homeworkIds.isEmpty()
                ? Collections.emptyList()
                : homeworkTaskMapper.selectList(
                new LambdaQueryWrapper<HomeworkTaskPo>()
                        .in(HomeworkTaskPo::getHomeworkId, homeworkIds)
                        .eq(HomeworkTaskPo::getIsDeleted, false)
        );
        if (query.getClassId() != null) {
            tasks = tasks.stream().filter(item -> Objects.equals(item.getClassId(), query.getClassId())).toList();
        }

        int totalTasks = tasks.size();
        int submittedCount = 0;
        int onTimeCount = 0;
        int reviewedCount = 0;
        int revisionRequiredCount = 0;
        Map<Long, HomeworkPo> homeworkMap = homeworks.stream().collect(Collectors.toMap(HomeworkPo::getId, Function.identity()));

        for (HomeworkTaskPo task : tasks) {
            String taskStatus = resolveTaskStatus(task, homeworkMap.get(task.getHomeworkId()));
            if (task.getLatestSubmissionId() != null) {
                submittedCount++;
                if (!Boolean.TRUE.equals(task.getIsLate())) {
                    onTimeCount++;
                }
            }
            if (!"unreviewed".equals(defaultString(task.getLatestReviewStatus(), "unreviewed"))) {
                reviewedCount++;
            }
            if ("revision_required".equals(taskStatus)) {
                revisionRequiredCount++;
            }
        }

        HomeworkOverviewStatsVo vo = new HomeworkOverviewStatsVo();
        vo.setPublishCount(homeworks.size());
        vo.setSubmissionRate(rate(submittedCount, totalTasks));
        vo.setOnTimeRate(rate(onTimeCount, submittedCount));
        vo.setReviewRate(rate(reviewedCount, submittedCount));
        vo.setRevisionRequiredRate(rate(revisionRequiredCount, reviewedCount));
        return vo;
    }

    private HomeworkTaskPo requireOwnedTask(Long teacherUserId, Long taskId) {
        HomeworkTaskPo task = homeworkTaskMapper.selectById(taskId);
        if (task == null || Boolean.TRUE.equals(task.getIsDeleted())) {
            throw new CommonException("Task not found");
        }
        requireOwnedHomework(teacherUserId, task.getHomeworkId());
        return task;
    }

    private Map<Long, StudentPo> loadStudentMap(Set<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return studentMapper.selectList(new LambdaQueryWrapper<StudentPo>().in(StudentPo::getId, studentIds)).stream()
                .collect(Collectors.toMap(StudentPo::getId, Function.identity(), (left, right) -> left));
    }

    private HomeworkTaskListItemVo toHomeworkTaskListItem(
            HomeworkTaskPo task,
            HomeworkPo homework,
            Map<Long, StudentPo> studentMap,
            Map<Long, UserPo> studentUserMap,
            Map<Long, SchoolClassPo> classMap
    ) {
        StudentPo student = studentMap.get(task.getStudentId());
        if (student == null) {
            return null;
        }
        UserPo user = studentUserMap.get(student.getStudentUserId());
        SchoolClassPo classPo = classMap.get(task.getClassId());

        HomeworkTaskListItemVo vo = new HomeworkTaskListItemVo();
        vo.setTaskId(task.getId());
        vo.setStudentId(task.getStudentId());
        vo.setStudentName(user == null ? defaultString(student.getStudentNo(), "Student") : user.getUserName());
        vo.setClassId(task.getClassId());
        vo.setClassName(classPo == null ? "Class" : classPo.getClassName());
        vo.setTaskStatus(resolveTaskStatus(task, homework));
        vo.setReviewStatus(defaultString(task.getLatestReviewStatus(), "unreviewed"));
        vo.setLatestSubmittedAt(task.getLatestSubmittedAt());
        vo.setSubmissionCount(defaultInteger(task.getSubmissionCount(), 0));
        vo.setIsLate(Boolean.TRUE.equals(task.getIsLate()));
        return vo;
    }

    private HomeworkPrintVo.StudentItem toPrintStudentItem(StudentPo student, Map<Long, UserPo> studentUserMap) {
        HomeworkPrintVo.StudentItem item = new HomeworkPrintVo.StudentItem();
        item.setStudentId(student.getId());
        item.setStudentNo(student.getStudentNo());
        item.setStudentName(resolveStudentName(student, studentUserMap));
        return item;
    }

    private String resolveStudentName(StudentPo student, Map<Long, UserPo> studentUserMap) {
        if (student == null) {
            return "Student";
        }
        UserPo user = studentUserMap.get(student.getStudentUserId());
        return user == null ? defaultString(student.getStudentNo(), "Student") : user.getUserName();
    }

    private void savePrintLog(Long teacherUserId, Long homeworkId) {
        OperationLogPo log = new OperationLogPo();
        log.setOperatorUserId(teacherUserId);
        log.setOperatorRole("teacher");
        log.setBizType("homework");
        log.setBizId(homeworkId);
        log.setActionType("homework_print");
        log.setRequestPayload("{}");
        log.setResultCode(0);
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private HomeworkAssetVo toAssetVo(HomeworkSubmissionAssetPo po) {
        HomeworkAssetVo vo = new HomeworkAssetVo();
        vo.setAssetType(po.getAssetType());
        vo.setAssetUrl(po.getAssetUrl());
        vo.setAssetName(po.getAssetName());
        vo.setAssetSize(po.getAssetSize());
        return vo;
    }

    private HomeworkAssetVo toAssetVo(HomeworkReviewAssetPo po) {
        HomeworkAssetVo vo = new HomeworkAssetVo();
        vo.setAssetType(po.getAssetType());
        vo.setAssetUrl(po.getAssetUrl());
        vo.setAssetName(po.getAssetUrl());
        return vo;
    }

    private String normalizeReviewStatus(String reviewStatus) {
        String normalized = defaultString(reviewStatus, "").trim();
        if ("completed".equals(normalized) || "revision_required".equals(normalized)) {
            return normalized;
        }
        throw new CommonException("reviewStatus must be completed or revision_required");
    }

    private double rate(int numerator, int denominator) {
        if (denominator <= 0 || numerator <= 0) {
            return 0D;
        }
        return BigDecimal.valueOf((double) numerator / denominator)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private UserPo resolveTeacherUser(String authorization) {
        return CurrentUserSupport.requireUser(authorization, "teacher", userMapper);
    }

    private boolean isActiveTeacher(UserPo user) {
        return CurrentUserSupport.isActiveUser(user, "teacher");
    }

    private SchoolClassPo requireSchoolClass(UserPo teacherUser, Long classId) {
        if (classId == null) {
            throw new CommonException("classId\u4e0d\u80fd\u4e3a\u7a7a");
        }
        SchoolClassPo classPo = schoolClassMapper.selectById(classId);
        if (classPo == null) {
            throw new CommonException("\u73ed\u7ea7\u4e0d\u5b58\u5728");
        }
        if (!Objects.equals(classPo.getSchoolId(), teacherUser.getSchoolId())) {
            throw new CommonException("\u4e0d\u80fd\u7ed1\u5b9a\u5176\u4ed6\u5b66\u6821\u7684\u73ed\u7ea7");
        }
        if (!"enabled".equalsIgnoreCase(defaultString(classPo.getStatus(), "enabled"))) {
            throw new CommonException("\u73ed\u7ea7\u5df2\u505c\u7528");
        }
        return classPo;
    }
    private HomeworkPo requireOwnedHomework(Long teacherUserId, Long homeworkId) {
        HomeworkPo homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new CommonException("Homework not found");
        }
        if (!Objects.equals(homework.getCreatorTeacherId(), teacherUserId)) {
            throw new CommonException("You cannot access this homework");
        }
        return homework;
    }

    private List<TeacherClassSubjectPo> listTeacherClassRelations(Long teacherUserId, String subjectCode) {
        LambdaQueryWrapper<TeacherClassSubjectPo> wrapper = new LambdaQueryWrapper<TeacherClassSubjectPo>()
                .eq(TeacherClassSubjectPo::getTeacherId, teacherUserId)
                .eq(TeacherClassSubjectPo::getStatus, "enabled")
                .orderByAsc(TeacherClassSubjectPo::getClassId)
                .orderByAsc(TeacherClassSubjectPo::getSubjectCode);
        if (StringUtils.hasText(subjectCode)) {
            wrapper.in(TeacherClassSubjectPo::getSubjectCode, subjectCodeAliases(normalizeSubjectCode(subjectCode)));
        }
        return teacherClassSubjectMapper.selectList(wrapper);
    }

    private String normalizeSubjectCode(String subjectCode) {
        if (!StringUtils.hasText(subjectCode)) {
            throw new CommonException("subjectCode\u4e0d\u80fd\u4e3a\u7a7a");
        }
        return canonicalSubjectCode(subjectCode);
    }

    private TeachingClassVo buildTeachingClassVo(
            TeacherClassSubjectPo relation,
            SchoolClassPo classPo,
            Map<Long, SchoolGradePo> gradeMap,
            Map<Long, SchoolPo> schoolMap,
            Map<Long, Integer> studentCountMap,
            Long teacherUserId
    ) {
        if (relation == null || classPo == null) {
            return null;
        }

        SchoolGradePo gradePo = gradeMap.get(classPo.getGradeId());
        SchoolPo schoolPo = schoolMap.get(classPo.getSchoolId());
        String subjectCode = canonicalSubjectCode(relation.getSubjectCode());

        TeachingClassVo vo = new TeachingClassVo();
        vo.setRelationId(relation.getId());
        vo.setClassId(classPo.getId());
        vo.setClassName(classPo.getClassName());
        vo.setGradeId(classPo.getGradeId());
        vo.setGradeName(gradePo == null ? null : gradePo.getGradeName());
        vo.setSchoolId(classPo.getSchoolId());
        vo.setSchoolName(schoolPo == null ? null : schoolPo.getSchoolName());
        vo.setSubjectCode(subjectCode);
        vo.setSubjectName(subjectName(subjectCode));
        vo.setStudentCount(studentCountMap.getOrDefault(classPo.getId(), 0));
        vo.setIsHeadTeacher(Boolean.TRUE.equals(relation.getIsHeadTeacher())
                || Objects.equals(classPo.getHomeroomTeacherId(), teacherUserId));
        vo.setStatus(defaultString(relation.getStatus(), defaultString(classPo.getStatus(), "enabled")));
        return vo;
    }

    private TeachingClassVo buildTeachingClassVo(
            SchoolClassPo classPo,
            String subjectCode,
            Long teacherUserId,
            Map<Long, SchoolGradePo> gradeMap,
            Map<Long, SchoolPo> schoolMap,
            Map<Long, Integer> studentCountMap
    ) {
        if (classPo == null) {
            return null;
        }

        SchoolGradePo gradePo = gradeMap.get(classPo.getGradeId());
        SchoolPo schoolPo = schoolMap.get(classPo.getSchoolId());

        TeachingClassVo vo = new TeachingClassVo();
        vo.setClassId(classPo.getId());
        vo.setClassName(classPo.getClassName());
        vo.setGradeId(classPo.getGradeId());
        vo.setGradeName(gradePo == null ? null : gradePo.getGradeName());
        vo.setSchoolId(classPo.getSchoolId());
        vo.setSchoolName(schoolPo == null ? null : schoolPo.getSchoolName());
        vo.setSubjectCode(subjectCode);
        vo.setSubjectName(subjectName(subjectCode));
        vo.setStudentCount(studentCountMap.getOrDefault(classPo.getId(), 0));
        vo.setIsHeadTeacher(Objects.equals(classPo.getHomeroomTeacherId(), teacherUserId));
        vo.setStatus(defaultString(classPo.getStatus(), "enabled"));
        return vo;
    }
    private void validateHomeworkSave(UserPo teacherUser, HomeworkSaveDto saveDto) {
        if (saveDto == null) {
            throw new CommonException("Request body is required");
        }
        if (saveDto.getDeadlineAt() != null && !saveDto.getDeadlineAt().isAfter(LocalDateTime.now())) {
            throw new CommonException("deadlineAt must be later than now");
        }

        Set<Long> allowedClassIds = listTeacherClassRelations(teacherUser.getId(), saveDto.getSubjectCode()).stream()
                .map(TeacherClassSubjectPo::getClassId)
                .collect(Collectors.toSet());
        Set<Long> requestedClassIds = new LinkedHashSet<>(defaultList(saveDto.getClassIds()));
        if (requestedClassIds.isEmpty()) {
            throw new CommonException("Please select at least one class");
        }
        if (!allowedClassIds.containsAll(requestedClassIds)) {
            throw new CommonException("Selected class is not assigned to current teacher or subject");
        }
    }

    private Map<Long, SchoolClassPo> loadClassMap(Set<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolClassMapper.selectList(
                        new LambdaQueryWrapper<SchoolClassPo>()
                                .in(SchoolClassPo::getId, classIds)
                                .eq(SchoolClassPo::getStatus, "enabled")
                ).stream()
                .collect(Collectors.toMap(SchoolClassPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, SchoolGradePo> loadGradeMap(Set<Long> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolGradeMapper.selectList(
                        new LambdaQueryWrapper<SchoolGradePo>()
                                .in(SchoolGradePo::getId, gradeIds)
                ).stream()
                .collect(Collectors.toMap(SchoolGradePo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, SchoolPo> loadSchoolMap(Set<Long> schoolIds) {
        if (schoolIds == null || schoolIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolMapper.selectList(
                        new LambdaQueryWrapper<SchoolPo>()
                                .in(SchoolPo::getId, schoolIds)
                ).stream()
                .collect(Collectors.toMap(SchoolPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, Integer> loadStudentCountMap(Set<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return studentMapper.selectList(
                        new LambdaQueryWrapper<StudentPo>()
                                .in(StudentPo::getClassId, classIds)
                                .eq(StudentPo::getStatus, "enabled")
                ).stream()
                .filter(item -> item.getClassId() != null)
                .collect(Collectors.toMap(
                        StudentPo::getClassId,
                        item -> 1,
                        Integer::sum,
                        LinkedHashMap::new
                ));
    }

    private Map<Long, UserPo> loadUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectList(
                        new LambdaQueryWrapper<UserPo>()
                                .in(UserPo::getId, userIds)
                ).stream()
                .collect(Collectors.toMap(UserPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private ClassCandidateVo buildClassCandidateVo(
            SchoolClassPo classPo,
            Map<Long, SchoolGradePo> gradeMap,
            Map<Long, SchoolPo> schoolMap,
            Map<Long, Integer> studentCountMap,
            List<TeacherClassSubjectPo> relations,
            Map<Long, UserPo> teacherUserMap
    ) {
        ClassCandidateVo vo = new ClassCandidateVo();
        SchoolGradePo gradePo = gradeMap.get(classPo.getGradeId());
        SchoolPo schoolPo = schoolMap.get(classPo.getSchoolId());
        vo.setClassId(classPo.getId());
        vo.setClassName(classPo.getClassName());
        vo.setGradeId(classPo.getGradeId());
        vo.setGradeName(gradePo == null ? null : gradePo.getGradeName());
        vo.setSchoolId(classPo.getSchoolId());
        vo.setSchoolName(schoolPo == null ? null : schoolPo.getSchoolName());
        vo.setStudentCount(studentCountMap.getOrDefault(classPo.getId(), 0));
        vo.setStatus(defaultString(classPo.getStatus(), "enabled"));
        vo.setSubjectBindings(defaultList(relations).stream()
                .map(item -> buildClassSubjectVo(item, teacherUserMap))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(item -> defaultString(item.getSubjectCode(), "")))
                .toList());
        return vo;
    }

    private ClassSubjectVo buildClassSubjectVo(TeacherClassSubjectPo relation, Map<Long, UserPo> teacherUserMap) {
        if (relation == null) {
            return null;
        }

        UserPo teacherUser = teacherUserMap.get(relation.getTeacherId());
        String subjectCode = canonicalSubjectCode(relation.getSubjectCode());

        ClassSubjectVo vo = new ClassSubjectVo();
        vo.setSubjectCode(subjectCode);
        vo.setSubjectName(subjectName(subjectCode));
        vo.setTeacherId(relation.getTeacherId());
        vo.setTeacherName(teacherUser == null ? null : teacherUser.getUserName());
        return vo;
    }

    private boolean matchesCandidateKeyword(ClassCandidateVo candidate, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return containsText(candidate.getClassName(), normalizedKeyword)
                || containsText(candidate.getGradeName(), normalizedKeyword)
                || containsText(candidate.getSchoolName(), normalizedKeyword);
    }

    private boolean containsText(String source, String keyword) {
        return StringUtils.hasText(source)
                && StringUtils.hasText(keyword)
                && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void syncHeadTeacherFlags(Long classId, Long headTeacherUserId) {
        if (classId == null) {
            return;
        }
        List<TeacherClassSubjectPo> relations = teacherClassSubjectMapper.selectList(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .eq(TeacherClassSubjectPo::getClassId, classId)
                        .eq(TeacherClassSubjectPo::getStatus, "enabled")
        );
        for (TeacherClassSubjectPo relation : relations) {
            relation.setIsHeadTeacher(headTeacherUserId != null && Objects.equals(relation.getTeacherId(), headTeacherUserId));
            teacherClassSubjectMapper.updateById(relation);
        }
    }

    private Set<String> subjectCodeAliases(String subjectCode) {
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
            return subjectCode;
        }
        String normalized = subjectCode.trim().toLowerCase(Locale.ROOT);
        if ("moral".equals(normalized)) {
            return "morality";
        }
        return normalized;
    }
    private Map<Long, List<HomeworkClassPo>> groupByHomeworkId(Set<Long> homeworkIds) {
        if (homeworkIds == null || homeworkIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return homeworkClassMapper.selectList(
                        new LambdaQueryWrapper<HomeworkClassPo>()
                                .in(HomeworkClassPo::getHomeworkId, homeworkIds)
                                .orderByAsc(HomeworkClassPo::getId)
                ).stream()
                .collect(Collectors.groupingBy(HomeworkClassPo::getHomeworkId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, List<HomeworkTaskPo>> groupTasksByHomeworkId(Set<Long> homeworkIds) {
        if (homeworkIds == null || homeworkIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return homeworkTaskMapper.selectList(
                        new LambdaQueryWrapper<HomeworkTaskPo>()
                                .in(HomeworkTaskPo::getHomeworkId, homeworkIds)
                                .eq(HomeworkTaskPo::getIsDeleted, false)
                ).stream()
                .collect(Collectors.groupingBy(HomeworkTaskPo::getHomeworkId));
    }

    private HomeworkListItemVo buildHomeworkListItem(
            HomeworkPo homework,
            List<HomeworkClassPo> relations,
            Map<Long, SchoolClassPo> classMap,
            List<HomeworkTaskPo> tasks,
            Map<Long, HomeworkPo> homeworkMap
    ) {
        HomeworkListItemVo vo = new HomeworkListItemVo();
        vo.setHomeworkId(homework.getId());
        vo.setTitle(homework.getTitle());
        vo.setSubjectCode(homework.getSubjectCode());
        vo.setSubjectName(subjectName(homework.getSubjectCode()));
        vo.setClassNames(defaultList(relations).stream()
                .map(HomeworkClassPo::getClassId)
                .map(classMap::get)
                .filter(Objects::nonNull)
                .map(SchoolClassPo::getClassName)
                .distinct()
                .toList());
        vo.setDeadlineAt(homework.getDeadlineAt());
        vo.setStatus(homework.getStatus());

        int submittedCount = 0;
        int pendingCount = 0;
        int revisionRequiredCount = 0;
        for (HomeworkTaskPo task : defaultList(tasks)) {
            String taskStatus = resolveTaskStatus(task, homeworkMap.get(task.getHomeworkId()));
            if (task.getLatestSubmissionId() != null) {
                submittedCount++;
            }
            if ("pending".equals(taskStatus) || "overdue".equals(taskStatus)) {
                pendingCount++;
            }
            if ("revision_required".equals(taskStatus)) {
                revisionRequiredCount++;
            }
        }

        vo.setSubmittedCount(submittedCount);
        vo.setPendingCount(pendingCount);
        vo.setRevisionRequiredCount(revisionRequiredCount);
        return vo;
    }

    private void replaceHomeworkClasses(Long homeworkId, List<Long> classIds) {
        homeworkClassMapper.delete(new LambdaQueryWrapper<HomeworkClassPo>().eq(HomeworkClassPo::getHomeworkId, homeworkId));
        for (Long classId : new LinkedHashSet<>(defaultList(classIds))) {
            if (classId == null) {
                continue;
            }
            HomeworkClassPo relation = new HomeworkClassPo();
            relation.setHomeworkId(homeworkId);
            relation.setClassId(classId);
            homeworkClassMapper.insert(relation);
        }
    }

    private void replaceHomeworkAttachments(Long homeworkId, List<HomeworkAssetDto> attachments) {
        homeworkAttachmentMapper.delete(new LambdaQueryWrapper<HomeworkAttachmentPo>().eq(HomeworkAttachmentPo::getHomeworkId, homeworkId));
        int sortNo = 1;
        for (HomeworkAssetDto assetDto : defaultList(attachments)) {
            if (!StringUtils.hasText(assetDto.getAssetUrl())) {
                continue;
            }
            HomeworkAttachmentPo assetPo = new HomeworkAttachmentPo();
            assetPo.setHomeworkId(homeworkId);
            assetPo.setAssetType(defaultString(assetDto.getAssetType(), "file"));
            assetPo.setAssetUrl(assetDto.getAssetUrl().trim());
            assetPo.setAssetName(trimToNull(assetDto.getAssetName()));
            assetPo.setAssetSize(assetDto.getAssetSize());
            assetPo.setSortNo(sortNo++);
            homeworkAttachmentMapper.insert(assetPo);
        }
    }

    private void syncHomeworkTasks(HomeworkPo homework) {
        List<HomeworkClassPo> homeworkClasses = homeworkClassMapper.selectList(
                new LambdaQueryWrapper<HomeworkClassPo>().eq(HomeworkClassPo::getHomeworkId, homework.getId())
        );
        Set<Long> classIds = homeworkClasses.stream().map(HomeworkClassPo::getClassId).collect(Collectors.toSet());
        if (classIds.isEmpty()) {
            return;
        }

        List<StudentPo> activeStudents = studentMapper.selectList(
                new LambdaQueryWrapper<StudentPo>()
                        .in(StudentPo::getClassId, classIds)
                        .eq(StudentPo::getStatus, "enabled")
        );
        List<HomeworkTaskPo> existingTasks = homeworkTaskMapper.selectList(
                new LambdaQueryWrapper<HomeworkTaskPo>().eq(HomeworkTaskPo::getHomeworkId, homework.getId())
        );
        Map<Long, HomeworkTaskPo> taskMap = existingTasks.stream()
                .collect(Collectors.toMap(HomeworkTaskPo::getStudentId, Function.identity(), (left, right) -> left));

        Set<Long> activeStudentIds = new LinkedHashSet<>();
        for (StudentPo student : activeStudents) {
            activeStudentIds.add(student.getId());
            HomeworkTaskPo task = taskMap.get(student.getId());
            if (task == null) {
                HomeworkTaskPo newTask = new HomeworkTaskPo();
                newTask.setHomeworkId(homework.getId());
                newTask.setStudentId(student.getId());
                newTask.setClassId(student.getClassId());
                newTask.setTaskStatus("pending");
                newTask.setLatestReviewStatus("unreviewed");
                newTask.setSubmissionCount(0);
                newTask.setIsLate(false);
                newTask.setIsDeleted(false);
                homeworkTaskMapper.insert(newTask);
                continue;
            }
            task.setClassId(student.getClassId());
            task.setIsDeleted(false);
            if (!StringUtils.hasText(task.getTaskStatus())) {
                task.setTaskStatus("pending");
            }
            if (!StringUtils.hasText(task.getLatestReviewStatus())) {
                task.setLatestReviewStatus("unreviewed");
            }
            homeworkTaskMapper.updateById(task);
        }

        for (HomeworkTaskPo task : existingTasks) {
            if (!activeStudentIds.contains(task.getStudentId())
                    && task.getLatestSubmissionId() == null
                    && defaultInteger(task.getSubmissionCount(), 0) == 0) {
                task.setIsDeleted(true);
                homeworkTaskMapper.updateById(task);
            }
        }
    }

    private HomeworkAssetVo toAssetVo(HomeworkAttachmentPo po) {
        HomeworkAssetVo vo = new HomeworkAssetVo();
        vo.setAssetType(po.getAssetType());
        vo.setAssetUrl(po.getAssetUrl());
        vo.setAssetName(po.getAssetName());
        vo.setAssetSize(po.getAssetSize());
        return vo;
    }

    private String resolveTaskStatus(HomeworkTaskPo task, HomeworkPo homework) {
        String taskStatus = defaultString(task.getTaskStatus(), "pending");
        if ("pending".equals(taskStatus)
                && homework != null
                && homework.getDeadlineAt() != null
                && LocalDateTime.now().isAfter(homework.getDeadlineAt())) {
            return "overdue";
        }
        return taskStatus;
    }

    private String joinSubmitTypes(List<String> submitTypes) {
        return new LinkedHashSet<>(defaultList(submitTypes)).stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private List<String> splitSubmitTypes(String submitTypeMask) {
        if (!StringUtils.hasText(submitTypeMask)) {
            return Collections.emptyList();
        }
        return List.of(submitTypeMask.split(",")).stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String subjectName(String subjectCode) {
        if (!StringUtils.hasText(subjectCode)) {
            return "Subject";
        }
        String normalized = canonicalSubjectCode(subjectCode);
        return SUBJECT_NAME_MAP.getOrDefault(normalized, subjectCode.trim());
    }
    private String trimToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
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

    private <T> PageDTO<T> emptyPage(Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        return PageDTO.of(Collections.emptyList(), 0, safePageNo, safePageSize);
    }

    private <T> PageSlice<T> slice(List<T> source, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 500);
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
