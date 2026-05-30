package com.primaryhomework.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.primaryhomework.backend.entity.dto.WrongBookAssetDto;
import com.primaryhomework.backend.entity.dto.WrongBookCreateDto;
import com.primaryhomework.backend.entity.dto.WrongBookFixDto;
import com.primaryhomework.backend.entity.dto.WrongBookPracticeSubmitDto;
import com.primaryhomework.backend.entity.dto.WrongBookPracticeSubmitItemDto;
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
import com.primaryhomework.backend.entity.po.WrongBookPracticeItemPo;
import com.primaryhomework.backend.entity.po.WrongBookPracticePo;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookAssetVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookDetailVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookListVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeDetailVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeHistoryVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeItemVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticePlanVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeSubmitResultVo;
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
import com.primaryhomework.backend.mapper.WrongBookPracticeItemMapper;
import com.primaryhomework.backend.mapper.WrongBookPracticeMapper;
import com.primaryhomework.backend.service.WrongBookService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.CurrentUserSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongBookServiceImpl implements WrongBookService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STATUS_PENDING_FIX = "pending_fix";
    private static final String STATUS_FIXED = "fixed";
    private static final String STATUS_MASTERED = "mastered";
    private static final String POOL_ACTIVE_WRONG = "active_wrong";
    private static final String POOL_RISKY_CORRECT = "risky_correct";
    private static final String POOL_MASTERED_ARCHIVE = "mastered_archive";
    private static final String RESULT_CORRECT = "correct";
    private static final String RESULT_WRONG = "wrong";
    private static final String RESULT_UNANSWERED = "unanswered";
    private static final BigDecimal DEFAULT_MASTERY_SCORE = new BigDecimal("100.00");
    private static final BigDecimal SCORE_CORRECT_DELTA = new BigDecimal("20.00");
    private static final BigDecimal SCORE_WRONG_DELTA = new BigDecimal("25.00");
    private static final BigDecimal MIN_RANDOM_WEIGHT = new BigDecimal("0.01");
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
    private final WrongBookPracticeMapper wrongBookPracticeMapper;
    private final WrongBookPracticeItemMapper wrongBookPracticeItemMapper;

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
            initPracticeState(itemPo, "teacher_mark");
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
        initPracticeState(itemPo, "student_manual");
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
        itemPo.setStatus(STATUS_FIXED);
        itemPo.setPoolType(defaultString(itemPo.getPoolType(), POOL_ACTIVE_WRONG));
        wrongBookItemMapper.updateById(itemPo);
        saveAssets(itemPo.getId(), fixDto.getAssets(), "correction_image");
    }

    @Override
    @Transactional
    public void markStudentWrongBookMastered(String authorization, Long wrongBookId) {
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        WrongBookItemPo itemPo = requireWrongBookForStudent(student.getId(), wrongBookId);
        itemPo.setStatus(STATUS_MASTERED);
        itemPo.setPoolType(POOL_MASTERED_ARCHIVE);
        wrongBookItemMapper.updateById(itemPo);
    }

    @Override
    public PageDTO<WrongBookListVo> pageParentWrongBooks(String authorization, Long studentId, WrongBookQueryDto queryDto) {
        UserPo parentUser = resolveParentUser(authorization);
        StudentPo student = requireBoundStudent(parentUser.getId(), studentId);
        return pageWrongBooks(student.getId(), queryDto);
    }

    @Override
    @Transactional
    public WrongBookPracticePlanVo generateStudentPracticePlan(String authorization, String subjectCode, Integer questionCount) {
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        String normalizedSubjectCode = canonicalSubjectCode(subjectCode);
        int safeQuestionCount = normalizeQuestionCount(questionCount);

        List<WrongBookItemPo> activePool = loadActivePracticePool(student.getId(), normalizedSubjectCode);
        List<WrongBookItemPo> riskyPool = loadRiskyPracticePool(student.getId(), normalizedSubjectCode);
        List<SelectedPracticeItem> selectedItems = selectPracticeItems(activePool, riskyPool, safeQuestionCount);
        if (selectedItems.isEmpty()) {
            throw new CommonException("当前没有可用于练习的错题");
        }

        LocalDateTime now = LocalDateTime.now();
        WrongBookPracticePo practicePo = new WrongBookPracticePo();
        practicePo.setStudentId(student.getId());
        practicePo.setPracticeName("\u4eca\u65e5\u9519\u9898\u5c0f\u7ec3\u4e60");
        practicePo.setPracticeType("smart_wrong_book");
        practicePo.setQuestionCount(selectedItems.size());
        practicePo.setWrongQuestionCount(countSelectedSource(selectedItems, POOL_ACTIVE_WRONG));
        practicePo.setRiskyQuestionCount(countSelectedSource(selectedItems, POOL_RISKY_CORRECT));
        practicePo.setSubmittedCount(0);
        practicePo.setCorrectCount(0);
        practicePo.setWrongCount(0);
        practicePo.setAccuracyRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        practicePo.setStatus("generated");
        practicePo.setGeneratedAt(now);
        wrongBookPracticeMapper.insert(practicePo);

        List<WrongBookPracticeItemPo> practiceItems = new ArrayList<>();
        int sortNo = 1;
        for (SelectedPracticeItem selectedItem : selectedItems) {
            WrongBookItemPo wrongBookItem = selectedItem.item();
            WrongBookPracticeItemPo itemPo = new WrongBookPracticeItemPo();
            itemPo.setPracticeId(practicePo.getId());
            itemPo.setStudentId(student.getId());
            itemPo.setWrongBookId(wrongBookItem.getId());
            itemPo.setQuestionNo(wrongBookItem.getQuestionNo());
            itemPo.setSubjectCode(wrongBookItem.getSubjectCode());
            itemPo.setQuestionText(wrongBookItem.getQuestionText());
            itemPo.setCorrectAnswer(wrongBookItem.getCorrectAnswer());
            itemPo.setItemSourceType(selectedItem.sourceType());
            itemPo.setItemWeight(itemWeight(wrongBookItem));
            itemPo.setResultStatus(RESULT_UNANSWERED);
            itemPo.setSortNo(sortNo++);
            wrongBookPracticeItemMapper.insert(itemPo);
            practiceItems.add(itemPo);
        }

        return toPracticePlanVo(practicePo, practiceItems);
    }

    @Override
    @Transactional
    public WrongBookPracticeSubmitResultVo submitStudentPractice(String authorization, WrongBookPracticeSubmitDto submitDto) {
        if (submitDto == null || submitDto.getPracticeId() == null) {
            throw new CommonException("practiceId不能为空");
        }
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        WrongBookPracticePo practicePo = requirePracticeForStudent(student.getId(), submitDto.getPracticeId());
        if ("completed".equalsIgnoreCase(defaultString(practicePo.getStatus(), ""))) {
            throw new CommonException("本次练习已经提交");
        }

        Map<Long, WrongBookPracticeSubmitItemDto> submittedItemMap = defaultList(submitDto.getItems()).stream()
                .filter(item -> item != null && item.getPracticeItemId() != null)
                .collect(Collectors.toMap(
                        WrongBookPracticeSubmitItemDto::getPracticeItemId,
                        Function.identity(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        if (submittedItemMap.isEmpty()) {
            throw new CommonException("练习提交明细不能为空");
        }

        List<WrongBookPracticeItemPo> practiceItems = wrongBookPracticeItemMapper.selectList(
                new LambdaQueryWrapper<WrongBookPracticeItemPo>()
                        .eq(WrongBookPracticeItemPo::getPracticeId, practicePo.getId())
                        .eq(WrongBookPracticeItemPo::getStudentId, student.getId())
                        .orderByAsc(WrongBookPracticeItemPo::getSortNo)
                        .orderByAsc(WrongBookPracticeItemPo::getId)
        );
        Map<Long, WrongBookPracticeItemPo> practiceItemMap = practiceItems.stream()
                .collect(Collectors.toMap(WrongBookPracticeItemPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        for (Long practiceItemId : submittedItemMap.keySet()) {
            if (!practiceItemMap.containsKey(practiceItemId)) {
                throw new CommonException("练习题目不属于本次练习");
            }
        }

        Set<Long> wrongBookIds = submittedItemMap.keySet().stream()
                .map(practiceItemMap::get)
                .filter(Objects::nonNull)
                .map(WrongBookPracticeItemPo::getWrongBookId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, WrongBookItemPo> wrongBookMap = wrongBookIds.isEmpty()
                ? Collections.emptyMap()
                : wrongBookItemMapper.selectBatchIds(wrongBookIds).stream()
                .filter(item -> Objects.equals(item.getStudentId(), student.getId()))
                .collect(Collectors.toMap(WrongBookItemPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        LocalDateTime now = LocalDateTime.now();
        int submittedCount = 0;
        int correctCount = 0;
        int wrongCount = 0;
        int masteredCount = 0;
        int returnedToActiveCount = 0;
        for (Map.Entry<Long, WrongBookPracticeSubmitItemDto> entry : submittedItemMap.entrySet()) {
            WrongBookPracticeItemPo practiceItem = practiceItemMap.get(entry.getKey());
            WrongBookPracticeSubmitItemDto submitItem = entry.getValue();
            if (submitItem.getWrongBookId() != null && !Objects.equals(submitItem.getWrongBookId(), practiceItem.getWrongBookId())) {
                throw new CommonException("错题记录与练习题目不匹配");
            }

            String resultStatus = normalizePracticeResult(submitItem, practiceItem);
            practiceItem.setStudentAnswer(trimToNull(submitItem.getStudentAnswer()));
            practiceItem.setResultStatus(resultStatus);
            practiceItem.setUsedDurationSeconds(nonNegativeInteger(submitItem.getUsedDurationSeconds()));
            practiceItem.setSubmittedAt(now);
            wrongBookPracticeItemMapper.updateById(practiceItem);

            if (RESULT_UNANSWERED.equals(resultStatus)) {
                continue;
            }
            submittedCount++;
            WrongBookItemPo wrongBookItem = wrongBookMap.get(practiceItem.getWrongBookId());
            if (wrongBookItem == null) {
                continue;
            }
            if (RESULT_CORRECT.equals(resultStatus)) {
                correctCount++;
                if (applyCorrectPracticeResult(wrongBookItem, now)) {
                    masteredCount++;
                }
            } else {
                wrongCount++;
                if (applyWrongPracticeResult(wrongBookItem, now)) {
                    returnedToActiveCount++;
                }
            }
            wrongBookItemMapper.updateById(wrongBookItem);
        }

        BigDecimal accuracyRate = submittedCount == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(correctCount).divide(BigDecimal.valueOf(submittedCount), 2, RoundingMode.HALF_UP);
        practicePo.setSubmittedCount(submittedCount);
        practicePo.setCorrectCount(correctCount);
        practicePo.setWrongCount(wrongCount);
        practicePo.setAccuracyRate(accuracyRate);
        practicePo.setStatus("completed");
        practicePo.setStartedAt(defaultDateTime(practicePo.getStartedAt(), practicePo.getGeneratedAt()));
        practicePo.setSubmittedAt(now);
        wrongBookPracticeMapper.updateById(practicePo);

        WrongBookPracticeSubmitResultVo resultVo = new WrongBookPracticeSubmitResultVo();
        resultVo.setPracticeId(practicePo.getId());
        resultVo.setCorrectCount(correctCount);
        resultVo.setWrongCount(wrongCount);
        resultVo.setAccuracyRate(accuracyRate);
        resultVo.setMasteredCount(masteredCount);
        resultVo.setReturnedToActiveCount(returnedToActiveCount);
        return resultVo;
    }

    @Override
    public PageDTO<WrongBookPracticeHistoryVo> pageStudentPracticeHistory(String authorization, Integer pageNo, Integer pageSize) {
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        List<WrongBookPracticePo> all = wrongBookPracticeMapper.selectList(
                new LambdaQueryWrapper<WrongBookPracticePo>()
                        .eq(WrongBookPracticePo::getStudentId, student.getId())
                        .orderByDesc(WrongBookPracticePo::getGeneratedAt)
                        .orderByDesc(WrongBookPracticePo::getId)
        );
        PageSlice<WrongBookPracticePo> slice = slice(all, pageNo, pageSize);
        return PageDTO.of(
                slice.items().stream().map(this::toPracticeHistoryVo).toList(),
                slice.total(),
                slice.pageNo(),
                slice.pageSize()
        );
    }

    @Override
    public WrongBookPracticeDetailVo getStudentPractice(String authorization, Long practiceId) {
        UserPo studentUser = resolveStudentUser(authorization);
        StudentPo student = requireStudent(studentUser.getId());
        WrongBookPracticePo practicePo = requirePracticeForStudent(student.getId(), practiceId);
        List<WrongBookPracticeItemPo> practiceItems = wrongBookPracticeItemMapper.selectList(
                new LambdaQueryWrapper<WrongBookPracticeItemPo>()
                        .eq(WrongBookPracticeItemPo::getPracticeId, practicePo.getId())
                        .eq(WrongBookPracticeItemPo::getStudentId, student.getId())
                        .orderByAsc(WrongBookPracticeItemPo::getSortNo)
                        .orderByAsc(WrongBookPracticeItemPo::getId)
        );
        return toPracticeDetailVo(practicePo, practiceItems);
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
        vo.setPoolType(defaultString(itemPo.getPoolType(), resolveDefaultPoolType(itemPo)));
        vo.setCorrectStreak(defaultInteger(itemPo.getCorrectStreak(), 0));
        vo.setPracticeCount(defaultInteger(itemPo.getPracticeCount(), 0));
        vo.setLastPracticeResult(itemPo.getLastPracticeResult());
        vo.setLastPracticedAt(formatTime(itemPo.getLastPracticedAt()));
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
        vo.setPoolType(defaultString(itemPo.getPoolType(), resolveDefaultPoolType(itemPo)));
        vo.setCorrectStreak(defaultInteger(itemPo.getCorrectStreak(), 0));
        vo.setPracticeCount(defaultInteger(itemPo.getPracticeCount(), 0));
        vo.setLastPracticeResult(itemPo.getLastPracticeResult());
        vo.setLastPracticedAt(formatTime(itemPo.getLastPracticedAt()));
        vo.setLastFixedText(itemPo.getLastFixedText());
        vo.setLastFixedAt(formatTime(itemPo.getLastFixedAt()));
        vo.setFixCount(defaultInteger(itemPo.getFixCount(), 0));
        vo.setAssets(defaultList(assets).stream().map(this::toWrongBookAssetVo).toList());
        return vo;
    }

    private WrongBookPracticePlanVo toPracticePlanVo(WrongBookPracticePo practicePo, List<WrongBookPracticeItemPo> items) {
        WrongBookPracticePlanVo vo = new WrongBookPracticePlanVo();
        vo.setPracticeId(practicePo.getId());
        vo.setPracticeName(practicePo.getPracticeName());
        vo.setQuestionCount(practicePo.getQuestionCount());
        vo.setWrongQuestionCount(practicePo.getWrongQuestionCount());
        vo.setRiskyQuestionCount(practicePo.getRiskyQuestionCount());
        vo.setItems(defaultList(items).stream().map(this::toPracticeItemVo).toList());
        return vo;
    }

    private WrongBookPracticeHistoryVo toPracticeHistoryVo(WrongBookPracticePo practicePo) {
        WrongBookPracticeHistoryVo vo = new WrongBookPracticeHistoryVo();
        vo.setPracticeId(practicePo.getId());
        vo.setPracticeName(practicePo.getPracticeName());
        vo.setPracticeType(practicePo.getPracticeType());
        vo.setQuestionCount(defaultInteger(practicePo.getQuestionCount(), 0));
        vo.setSubmittedCount(defaultInteger(practicePo.getSubmittedCount(), 0));
        vo.setCorrectCount(defaultInteger(practicePo.getCorrectCount(), 0));
        vo.setWrongCount(defaultInteger(practicePo.getWrongCount(), 0));
        vo.setAccuracyRate(defaultBigDecimal(practicePo.getAccuracyRate(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        vo.setStatus(practicePo.getStatus());
        vo.setGeneratedAt(formatTime(practicePo.getGeneratedAt()));
        vo.setSubmittedAt(formatTime(practicePo.getSubmittedAt()));
        return vo;
    }

    private WrongBookPracticeDetailVo toPracticeDetailVo(WrongBookPracticePo practicePo, List<WrongBookPracticeItemPo> items) {
        WrongBookPracticeDetailVo vo = new WrongBookPracticeDetailVo();
        vo.setPracticeId(practicePo.getId());
        vo.setPracticeName(practicePo.getPracticeName());
        vo.setPracticeType(practicePo.getPracticeType());
        vo.setQuestionCount(defaultInteger(practicePo.getQuestionCount(), 0));
        vo.setWrongQuestionCount(defaultInteger(practicePo.getWrongQuestionCount(), 0));
        vo.setRiskyQuestionCount(defaultInteger(practicePo.getRiskyQuestionCount(), 0));
        vo.setSubmittedCount(defaultInteger(practicePo.getSubmittedCount(), 0));
        vo.setCorrectCount(defaultInteger(practicePo.getCorrectCount(), 0));
        vo.setWrongCount(defaultInteger(practicePo.getWrongCount(), 0));
        vo.setAccuracyRate(defaultBigDecimal(practicePo.getAccuracyRate(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        vo.setStatus(practicePo.getStatus());
        vo.setGeneratedAt(formatTime(practicePo.getGeneratedAt()));
        vo.setSubmittedAt(formatTime(practicePo.getSubmittedAt()));
        vo.setItems(defaultList(items).stream().map(this::toPracticeItemVo).toList());
        return vo;
    }

    private WrongBookPracticeItemVo toPracticeItemVo(WrongBookPracticeItemPo itemPo) {
        WrongBookPracticeItemVo vo = new WrongBookPracticeItemVo();
        vo.setPracticeItemId(itemPo.getId());
        vo.setWrongBookId(itemPo.getWrongBookId());
        vo.setQuestionNo(itemPo.getQuestionNo());
        vo.setSubjectCode(itemPo.getSubjectCode());
        vo.setSubjectName(subjectName(itemPo.getSubjectCode()));
        vo.setQuestionText(itemPo.getQuestionText());
        vo.setCorrectAnswer(itemPo.getCorrectAnswer());
        vo.setStudentAnswer(itemPo.getStudentAnswer());
        vo.setItemSourceType(itemPo.getItemSourceType());
        vo.setItemWeight(itemPo.getItemWeight());
        vo.setResultStatus(itemPo.getResultStatus());
        vo.setUsedDurationSeconds(itemPo.getUsedDurationSeconds());
        vo.setSortNo(itemPo.getSortNo());
        vo.setSubmittedAt(formatTime(itemPo.getSubmittedAt()));
        return vo;
    }

    private List<WrongBookItemPo> loadActivePracticePool(Long studentId, String subjectCode) {
        LambdaQueryWrapper<WrongBookItemPo> wrapper = new LambdaQueryWrapper<WrongBookItemPo>()
                .eq(WrongBookItemPo::getStudentId, studentId)
                .and(query -> query.eq(WrongBookItemPo::getPoolType, POOL_ACTIVE_WRONG)
                        .or(nested -> nested.isNull(WrongBookItemPo::getPoolType)
                                .ne(WrongBookItemPo::getStatus, STATUS_MASTERED)));
        if (StringUtils.hasText(subjectCode)) {
            wrapper.eq(WrongBookItemPo::getSubjectCode, subjectCode);
        }
        return sortedPracticeCandidates(wrongBookItemMapper.selectList(wrapper));
    }

    private List<WrongBookItemPo> loadRiskyPracticePool(Long studentId, String subjectCode) {
        LambdaQueryWrapper<WrongBookItemPo> wrapper = new LambdaQueryWrapper<WrongBookItemPo>()
                .eq(WrongBookItemPo::getStudentId, studentId)
                .ne(WrongBookItemPo::getStatus, STATUS_MASTERED)
                .and(query -> query.eq(WrongBookItemPo::getPoolType, POOL_RISKY_CORRECT)
                        .or(nested -> nested.eq(WrongBookItemPo::getLastPracticeResult, RESULT_CORRECT)
                                .ge(WrongBookItemPo::getLastPracticedAt, LocalDateTime.now().minusDays(7))));
        if (StringUtils.hasText(subjectCode)) {
            wrapper.eq(WrongBookItemPo::getSubjectCode, subjectCode);
        }
        return sortedPracticeCandidates(wrongBookItemMapper.selectList(wrapper));
    }

    private List<WrongBookItemPo> sortedPracticeCandidates(List<WrongBookItemPo> items) {
        return defaultList(items).stream()
                .sorted(Comparator
                        .comparing(this::itemWeight, Comparator.reverseOrder())
                        .thenComparing(item -> item.getLastPracticedAt() == null ? LocalDateTime.MIN : item.getLastPracticedAt())
                        .thenComparing(WrongBookItemPo::getId))
                .toList();
    }

    private List<SelectedPracticeItem> selectPracticeItems(List<WrongBookItemPo> activePool,
                                                           List<WrongBookItemPo> riskyPool,
                                                           int questionCount) {
        int riskyTarget = questionCount / 5;
        int activeTarget = questionCount - riskyTarget;
        List<SelectedPracticeItem> selectedItems = new ArrayList<>();
        Set<Long> selectedIds = new LinkedHashSet<>();

        takePracticeItems(activePool, POOL_ACTIVE_WRONG, activeTarget, selectedIds, selectedItems);
        takePracticeItems(riskyPool, POOL_RISKY_CORRECT, riskyTarget, selectedIds, selectedItems);
        takePracticeItems(activePool, POOL_ACTIVE_WRONG, questionCount - selectedItems.size(), selectedIds, selectedItems);
        takePracticeItems(riskyPool, POOL_RISKY_CORRECT, questionCount - selectedItems.size(), selectedIds, selectedItems);
        return selectedItems;
    }

    private void takePracticeItems(List<WrongBookItemPo> source,
                                   String sourceType,
                                   int limit,
                                   Set<Long> selectedIds,
                                   List<SelectedPracticeItem> selectedItems) {
        if (limit <= 0) {
            return;
        }
        List<WrongBookItemPo> availableItems = defaultList(source).stream()
                .filter(item -> item != null && item.getId() != null && !selectedIds.contains(item.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        int remaining = Math.min(limit, availableItems.size());
        while (remaining > 0 && !availableItems.isEmpty()) {
            WrongBookItemPo selectedItem = removeWeightedRandomItem(availableItems);
            if (selectedItem == null || !selectedIds.add(selectedItem.getId())) {
                continue;
            }
            selectedItems.add(new SelectedPracticeItem(selectedItem, sourceType));
            remaining--;
        }
    }

    private WrongBookItemPo removeWeightedRandomItem(List<WrongBookItemPo> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        BigDecimal totalWeight = candidates.stream()
                .map(this::effectivePracticeWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return candidates.remove(ThreadLocalRandom.current().nextInt(candidates.size()));
        }

        double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight.doubleValue());
        double cumulativeWeight = 0D;
        for (int i = 0; i < candidates.size(); i++) {
            cumulativeWeight += effectivePracticeWeight(candidates.get(i)).doubleValue();
            if (randomValue < cumulativeWeight || i == candidates.size() - 1) {
                return candidates.remove(i);
            }
        }

        return candidates.remove(candidates.size() - 1);
    }

    private int countSelectedSource(List<SelectedPracticeItem> selectedItems, String sourceType) {
        return (int) defaultList(selectedItems).stream()
                .filter(item -> sourceType.equals(item.sourceType()))
                .count();
    }

    private boolean applyCorrectPracticeResult(WrongBookItemPo itemPo, LocalDateTime now) {
        int correctStreak = defaultInteger(itemPo.getCorrectStreak(), 0) + 1;
        BigDecimal masteryScore = itemWeight(itemPo).subtract(SCORE_CORRECT_DELTA).max(BigDecimal.ZERO);
        itemPo.setCorrectStreak(correctStreak);
        itemPo.setPracticeCount(defaultInteger(itemPo.getPracticeCount(), 0) + 1);
        itemPo.setLastPracticeResult(RESULT_CORRECT);
        itemPo.setLastPracticedAt(now);
        itemPo.setMasteryScore(masteryScore);
        if (correctStreak >= 2) {
            itemPo.setPoolType(POOL_MASTERED_ARCHIVE);
            itemPo.setStatus(STATUS_MASTERED);
            return true;
        }
        itemPo.setPoolType(POOL_RISKY_CORRECT);
        return false;
    }

    private boolean applyWrongPracticeResult(WrongBookItemPo itemPo, LocalDateTime now) {
        boolean wasMastered = STATUS_MASTERED.equalsIgnoreCase(defaultString(itemPo.getStatus(), ""))
                || POOL_MASTERED_ARCHIVE.equals(itemPo.getPoolType());
        boolean returnedToActive = wasMastered || POOL_RISKY_CORRECT.equals(itemPo.getPoolType());
        itemPo.setCorrectStreak(0);
        itemPo.setPracticeCount(defaultInteger(itemPo.getPracticeCount(), 0) + 1);
        itemPo.setLastPracticeResult(RESULT_WRONG);
        itemPo.setLastPracticedAt(now);
        itemPo.setMasteryScore(itemWeight(itemPo).add(SCORE_WRONG_DELTA));
        itemPo.setPoolType(POOL_ACTIVE_WRONG);
        if (wasMastered) {
            itemPo.setStatus(STATUS_FIXED);
        } else if (!StringUtils.hasText(itemPo.getStatus())) {
            itemPo.setStatus(STATUS_PENDING_FIX);
        }
        return returnedToActive;
    }

    private String normalizePracticeResult(WrongBookPracticeSubmitItemDto submitItem, WrongBookPracticeItemPo practiceItem) {
        String resultStatus = trimToNull(submitItem.getResultStatus());
        if (RESULT_CORRECT.equalsIgnoreCase(resultStatus)) {
            return RESULT_CORRECT;
        }
        if (RESULT_WRONG.equalsIgnoreCase(resultStatus)) {
            return RESULT_WRONG;
        }
        if (RESULT_UNANSWERED.equalsIgnoreCase(resultStatus)) {
            return RESULT_UNANSWERED;
        }
        String studentAnswer = trimToNull(submitItem.getStudentAnswer());
        if (!StringUtils.hasText(studentAnswer)) {
            return RESULT_UNANSWERED;
        }
        String correctAnswer = trimToNull(practiceItem.getCorrectAnswer());
        if (StringUtils.hasText(correctAnswer) && correctAnswer.equalsIgnoreCase(studentAnswer)) {
            return RESULT_CORRECT;
        }
        return RESULT_WRONG;
    }

    private WrongBookPracticePo requirePracticeForStudent(Long studentId, Long practiceId) {
        if (practiceId == null) {
            throw new CommonException("practiceId不能为空");
        }
        WrongBookPracticePo practicePo = wrongBookPracticeMapper.selectById(practiceId);
        if (practicePo == null || !Objects.equals(practicePo.getStudentId(), studentId)) {
            throw new CommonException("练习记录不存在");
        }
        return practicePo;
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

    private void initPracticeState(WrongBookItemPo itemPo, String sourceScene) {
        itemPo.setPoolType(POOL_ACTIVE_WRONG);
        itemPo.setCorrectStreak(0);
        itemPo.setMasteryScore(DEFAULT_MASTERY_SCORE);
        itemPo.setPracticeCount(0);
        itemPo.setLastPracticeResult(null);
        itemPo.setSourceScene(sourceScene);
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

    private int normalizeQuestionCount(Integer questionCount) {
        if (questionCount == null || questionCount < 1) {
            return 10;
        }
        return Math.min(questionCount, 50);
    }

    private BigDecimal itemWeight(WrongBookItemPo itemPo) {
        return defaultBigDecimal(itemPo == null ? null : itemPo.getMasteryScore(), DEFAULT_MASTERY_SCORE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal effectivePracticeWeight(WrongBookItemPo itemPo) {
        return itemWeight(itemPo).max(MIN_RANDOM_WEIGHT);
    }

    private String resolveDefaultPoolType(WrongBookItemPo itemPo) {
        if (itemPo == null) {
            return POOL_ACTIVE_WRONG;
        }
        if (STATUS_MASTERED.equalsIgnoreCase(defaultString(itemPo.getStatus(), ""))) {
            return POOL_MASTERED_ARCHIVE;
        }
        return POOL_ACTIVE_WRONG;
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

    private Integer nonNegativeInteger(Integer value) {
        if (value == null) {
            return null;
        }
        return Math.max(0, value);
    }

    private BigDecimal defaultBigDecimal(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue : value;
    }

    private LocalDateTime defaultDateTime(LocalDateTime value, LocalDateTime defaultValue) {
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

    private record SelectedPracticeItem(WrongBookItemPo item, String sourceType) {
    }
}
