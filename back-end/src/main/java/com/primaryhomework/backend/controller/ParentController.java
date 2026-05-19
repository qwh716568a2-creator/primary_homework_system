package com.primaryhomework.backend.controller;

import com.primaryhomework.backend.entity.dto.WrongBookQueryDto;
import com.primaryhomework.backend.entity.dto.mobile.NotificationSettingsDto;
import com.primaryhomework.backend.entity.dto.mobile.PasswordCheckDto;
import com.primaryhomework.backend.entity.dto.mobile.SecuritySettingsDto;
import com.primaryhomework.backend.entity.dto.mobile.SubmitDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.R;
import com.primaryhomework.backend.entity.vo.mobile.ChildVo;
import com.primaryhomework.backend.entity.vo.mobile.HomeworkVo;
import com.primaryhomework.backend.entity.vo.mobile.MessageVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookListVo;
import com.primaryhomework.backend.service.MobilePreferenceService;
import com.primaryhomework.backend.service.ParentService;
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
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final MobilePreferenceService mobilePreferenceService;
    private final ParentService parentService;

    @GetMapping("/students")
    public R<List<ChildVo>> listStudents(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(parentService.listStudents(authorization));
    }

    @GetMapping("/students/{studentId}/homeworks")
    public R<List<HomeworkVo>> listHomeworks(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long studentId,
            @RequestParam(required = false) String tab
    ) {
        return R.ok(parentService.listHomeworks(authorization, studentId, tab));
    }

    @GetMapping("/students/{studentId}/homeworks/{homeworkId}")
    public R<HomeworkVo> getHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long studentId,
            @PathVariable Long homeworkId
    ) {
        return R.ok(parentService.getHomework(authorization, studentId, homeworkId));
    }

    @PostMapping("/students/{studentId}/homeworks/{homeworkId}/submissions")
    public R<Void> submitHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long studentId,
            @PathVariable Long homeworkId,
            @Valid @RequestBody SubmitDto submitDto
    ) {
        submitDto.setStudentId(studentId);
        parentService.assistSubmit(authorization, homeworkId, submitDto);
        return R.ok();
    }

    @PostMapping("/homeworks/{homeworkId}/assist-submit")
    public R<Void> assistSubmit(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId,
            @RequestParam(required = false) Long studentId,
            @Valid @RequestBody SubmitDto submitDto
    ) {
        if (submitDto.getStudentId() == null) {
            submitDto.setStudentId(studentId);
        }
        parentService.assistSubmit(authorization, homeworkId, submitDto);
        return R.ok();
    }

    @GetMapping("/notifications")
    public R<PageDTO<MessageVo>> listNotifications(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false, defaultValue = "all") String readStatus,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        return R.ok(parentService.listNotifications(authorization, readStatus, pageNo, pageSize));
    }

    @GetMapping("/notifications/{notificationId}")
    public R<MessageVo> getNotification(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long notificationId
    ) {
        return R.ok(parentService.getNotification(authorization, notificationId));
    }

    @PostMapping("/notifications/{notificationId}/read")
    public R<Void> markNotificationRead(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long notificationId
    ) {
        parentService.markNotificationRead(authorization, notificationId);
        return R.ok();
    }

    @GetMapping("/settings/messages")
    public R<NotificationSettingsDto> getNotificationSettings(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(mobilePreferenceService.getNotificationSettings(authorization, "parent"));
    }

    @PostMapping("/settings/messages")
    public R<NotificationSettingsDto> saveNotificationSettings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody NotificationSettingsDto dto
    ) {
        return R.ok(mobilePreferenceService.saveNotificationSettings(authorization, "parent", dto));
    }

    @PostMapping("/settings/messages/reset")
    public R<NotificationSettingsDto> resetNotificationSettings(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(mobilePreferenceService.resetNotificationSettings(authorization, "parent"));
    }

    @PostMapping("/settings/messages/read-all")
    public R<Void> markAllNotificationsRead(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        mobilePreferenceService.markAllRead(authorization, "parent");
        return R.ok();
    }

    @GetMapping("/settings/security")
    public R<SecuritySettingsDto> getSecuritySettings(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(mobilePreferenceService.getSecuritySettings(authorization, "parent"));
    }

    @PostMapping("/settings/security")
    public R<SecuritySettingsDto> saveSecuritySettings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody SecuritySettingsDto dto
    ) {
        return R.ok(mobilePreferenceService.saveSecuritySettings(authorization, "parent", dto));
    }

    @PostMapping("/settings/security/reset")
    public R<SecuritySettingsDto> resetSecuritySettings(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(mobilePreferenceService.resetSecuritySettings(authorization, "parent"));
    }

    @PostMapping("/settings/security/password-check")
    public R<SecuritySettingsDto> checkPassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody PasswordCheckDto dto
    ) {
        return R.ok(mobilePreferenceService.checkPassword(authorization, "parent", dto));
    }

    @GetMapping("/students/{studentId}/wrong-book")
    public R<PageDTO<WrongBookListVo>> listWrongBooks(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long studentId,
            WrongBookQueryDto queryDto
    ) {
        return R.ok(parentService.listWrongBooks(authorization, studentId, queryDto));
    }
}
