package com.primaryhomework.backend.controller;

import com.primaryhomework.backend.entity.dto.WrongBookCreateDto;
import com.primaryhomework.backend.entity.dto.WrongBookFixDto;
import com.primaryhomework.backend.entity.dto.WrongBookPracticeSubmitDto;
import com.primaryhomework.backend.entity.dto.WrongBookQueryDto;
import com.primaryhomework.backend.entity.dto.mobile.NotificationSettingsDto;
import com.primaryhomework.backend.entity.dto.mobile.PasswordCheckDto;
import com.primaryhomework.backend.entity.dto.mobile.SecuritySettingsDto;
import com.primaryhomework.backend.entity.dto.mobile.SubmitDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.R;
import com.primaryhomework.backend.entity.vo.mobile.HomeworkVo;
import com.primaryhomework.backend.entity.vo.mobile.MessageVo;
import com.primaryhomework.backend.entity.vo.mobile.ReviewVo;
import com.primaryhomework.backend.entity.vo.mobile.SubmissionVo;
import com.primaryhomework.backend.entity.vo.mobile.SubjectOptionVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookDetailVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookListVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeDetailVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeHistoryVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticePlanVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeSubmitResultVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookSaveVo;
import com.primaryhomework.backend.service.MobilePreferenceService;
import com.primaryhomework.backend.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final MobilePreferenceService mobilePreferenceService;
    private final StudentService studentService;

    @GetMapping("/homeworks")
    public R<List<HomeworkVo>> listHomeworks(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String tab
    ) {
        return R.ok(studentService.listHomeworks(authorization, tab));
    }

    @GetMapping("/homeworks/{homeworkId}")
    public R<HomeworkVo> getHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId
    ) {
        return R.ok(studentService.getHomework(authorization, homeworkId));
    }

    @PostMapping("/homeworks/{homeworkId}/submit")
    public R<Void> submitHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId,
            @Valid @RequestBody SubmitDto submitDto
    ) {
        studentService.submitHomework(authorization, homeworkId, submitDto);
        return R.ok();
    }

    @PostMapping("/homeworks/{homeworkId}/submissions")
    public R<Void> submitHomeworkDoc(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId,
            @Valid @RequestBody SubmitDto submitDto
    ) {
        studentService.submitHomework(authorization, homeworkId, submitDto);
        return R.ok();
    }

    @GetMapping("/homeworks/{homeworkId}/submissions")
    public R<List<SubmissionVo>> listSubmissions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId
    ) {
        return R.ok(studentService.listSubmissions(authorization, homeworkId));
    }

    @GetMapping("/homeworks/{homeworkId}/reviews")
    public R<List<ReviewVo>> listReviews(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId
    ) {
        return R.ok(studentService.listReviews(authorization, homeworkId));
    }

    @GetMapping("/notifications")
    public R<PageDTO<MessageVo>> listNotifications(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false, defaultValue = "all") String readStatus,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        return R.ok(studentService.listNotifications(authorization, readStatus, pageNo, pageSize));
    }

    @GetMapping("/notifications/{notificationId}")
    public R<MessageVo> getNotification(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long notificationId
    ) {
        return R.ok(studentService.getNotification(authorization, notificationId));
    }

    @PostMapping("/notifications/{notificationId}/read")
    public R<Void> markNotificationRead(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long notificationId
    ) {
        studentService.markNotificationRead(authorization, notificationId);
        return R.ok();
    }

    @GetMapping("/settings/messages")
    public R<NotificationSettingsDto> getNotificationSettings(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(mobilePreferenceService.getNotificationSettings(authorization, "student"));
    }

    @PostMapping("/settings/messages")
    public R<NotificationSettingsDto> saveNotificationSettings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody NotificationSettingsDto dto
    ) {
        return R.ok(mobilePreferenceService.saveNotificationSettings(authorization, "student", dto));
    }

    @PostMapping("/settings/messages/reset")
    public R<NotificationSettingsDto> resetNotificationSettings(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(mobilePreferenceService.resetNotificationSettings(authorization, "student"));
    }

    @PostMapping("/settings/messages/read-all")
    public R<Void> markAllNotificationsRead(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        mobilePreferenceService.markAllRead(authorization, "student");
        return R.ok();
    }

    @GetMapping("/settings/security")
    public R<SecuritySettingsDto> getSecuritySettings(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(mobilePreferenceService.getSecuritySettings(authorization, "student"));
    }

    @PostMapping("/settings/security")
    public R<SecuritySettingsDto> saveSecuritySettings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody SecuritySettingsDto dto
    ) {
        return R.ok(mobilePreferenceService.saveSecuritySettings(authorization, "student", dto));
    }

    @PostMapping("/settings/security/reset")
    public R<SecuritySettingsDto> resetSecuritySettings(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(mobilePreferenceService.resetSecuritySettings(authorization, "student"));
    }

    @PostMapping("/settings/security/password-check")
    public R<SecuritySettingsDto> checkPassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody PasswordCheckDto dto
    ) {
        return R.ok(mobilePreferenceService.checkPassword(authorization, "student", dto));
    }

    @GetMapping({"/wrong-book/subjects", "/subjects"})
    public R<List<SubjectOptionVo>> listWrongBookSubjects(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(studentService.listWrongBookSubjects(authorization));
    }

    @GetMapping("/wrong-book")
    public R<PageDTO<WrongBookListVo>> listWrongBooks(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            WrongBookQueryDto queryDto
    ) {
        return R.ok(studentService.listWrongBooks(authorization, queryDto));
    }

    @GetMapping("/wrong-book/{wrongBookId}")
    public R<WrongBookDetailVo> getWrongBook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long wrongBookId
    ) {
        return R.ok(studentService.getWrongBook(authorization, wrongBookId));
    }

    @PostMapping("/wrong-book")
    public R<WrongBookSaveVo> createWrongBook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody WrongBookCreateDto createDto
    ) {
        return R.ok(studentService.createWrongBook(authorization, createDto));
    }

    @PostMapping("/wrong-book/{wrongBookId}/fix")
    public R<Void> fixWrongBook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long wrongBookId,
            @Valid @RequestBody WrongBookFixDto fixDto
    ) {
        studentService.fixWrongBook(authorization, wrongBookId, fixDto);
        return R.ok();
    }

    @PostMapping("/wrong-book/{wrongBookId}/mastered")
    public R<Void> markWrongBookMastered(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long wrongBookId
    ) {
        studentService.markWrongBookMastered(authorization, wrongBookId);
        return R.ok();
    }

    @GetMapping("/wrong-book/practice/plan")
    public R<WrongBookPracticePlanVo> generateWrongBookPracticePlan(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) Integer questionCount
    ) {
        return R.ok(studentService.generateWrongBookPracticePlan(authorization, subjectCode, questionCount));
    }

    @PostMapping("/wrong-book/practice/submit")
    public R<WrongBookPracticeSubmitResultVo> submitWrongBookPractice(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody WrongBookPracticeSubmitDto submitDto
    ) {
        return R.ok(studentService.submitWrongBookPractice(authorization, submitDto));
    }

    @GetMapping("/wrong-book/practice/history")
    public R<PageDTO<WrongBookPracticeHistoryVo>> listWrongBookPracticeHistory(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        return R.ok(studentService.listWrongBookPracticeHistory(authorization, pageNo, pageSize));
    }

    @GetMapping("/wrong-book/practice/{practiceId}")
    public R<WrongBookPracticeDetailVo> getWrongBookPractice(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long practiceId
    ) {
        return R.ok(studentService.getWrongBookPractice(authorization, practiceId));
    }
}
