package com.primaryhomework.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.primaryhomework.backend.entity.dto.mobile.NotificationSettingsDto;
import com.primaryhomework.backend.entity.dto.mobile.PasswordCheckDto;
import com.primaryhomework.backend.entity.dto.mobile.SecuritySettingsDto;
import com.primaryhomework.backend.entity.po.MobilePreferencePo;
import com.primaryhomework.backend.entity.po.NotificationPo;
import com.primaryhomework.backend.entity.po.ParentPo;
import com.primaryhomework.backend.entity.po.StudentPo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.mapper.MobilePreferenceMapper;
import com.primaryhomework.backend.mapper.NotificationMapper;
import com.primaryhomework.backend.mapper.ParentMapper;
import com.primaryhomework.backend.mapper.StudentMapper;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.service.MobilePreferenceService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.CurrentUserSupport;
import com.primaryhomework.backend.utils.PasswordSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MobilePreferenceServiceImpl implements MobilePreferenceService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MobilePreferenceMapper mobilePreferenceMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final ParentMapper parentMapper;

    @Override
    public NotificationSettingsDto getNotificationSettings(String authorization, String roleType) {
        UserPo user = resolveUser(authorization, roleType);
        return buildNotificationDto(requirePreference(user, roleType));
    }

    @Override
    @Transactional
    public NotificationSettingsDto saveNotificationSettings(String authorization, String roleType, NotificationSettingsDto dto) {
        UserPo user = resolveUser(authorization, roleType);
        MobilePreferencePo preference = requirePreference(user, roleType);
        preference.setMasterEnabled(dto.getMasterEnabled());
        preference.setAssignmentEnabled(dto.getAssignmentEnabled());
        preference.setReviewEnabled(dto.getReviewEnabled());
        preference.setReminderEnabled(dto.getReminderEnabled());
        preference.setSystemEnabled(dto.getSystemEnabled());
        preference.setSoundEnabled(dto.getSoundEnabled());
        preference.setVibrationEnabled(dto.getVibrationEnabled());
        preference.setQuietHoursEnabled(dto.getQuietHoursEnabled());
        preference.setQuietStart(text(dto.getQuietStart()));
        preference.setQuietEnd(text(dto.getQuietEnd()));
        mobilePreferenceMapper.updateById(preference);
        return buildNotificationDto(preference);
    }

    @Override
    @Transactional
    public NotificationSettingsDto resetNotificationSettings(String authorization, String roleType) {
        UserPo user = resolveUser(authorization, roleType);
        MobilePreferencePo preference = requirePreference(user, roleType);
        applyDefaultNotification(preference);
        mobilePreferenceMapper.updateById(preference);
        return buildNotificationDto(preference);
    }

    @Override
    @Transactional
    public void markAllRead(String authorization, String roleType) {
        UserPo user = resolveUser(authorization, roleType);
        List<NotificationPo> notifications = notificationMapper.selectList(new LambdaQueryWrapper<NotificationPo>()
                .eq(NotificationPo::getReceiverUserId, user.getId())
                .eq(NotificationPo::getReceiverRole, roleType.toLowerCase())
                .isNull(NotificationPo::getReadAt));
        if (notifications.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (NotificationPo notification : notifications) {
            notification.setReadAt(now);
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public SecuritySettingsDto getSecuritySettings(String authorization, String roleType) {
        UserPo user = resolveUser(authorization, roleType);
        return buildSecurityDto(requirePreference(user, roleType));
    }

    @Override
    @Transactional
    public SecuritySettingsDto saveSecuritySettings(String authorization, String roleType, SecuritySettingsDto dto) {
        UserPo user = resolveUser(authorization, roleType);
        MobilePreferencePo preference = requirePreference(user, roleType);
        preference.setHideAccountIdentifier(dto.getHideAccountIdentifier());
        preference.setRememberAccount(dto.getRememberAccount());
        preference.setLoginAlertEnabled(dto.getLoginAlertEnabled());
        preference.setAppLockEnabled(dto.getAppLockEnabled());
        preference.setBiometricEnabled(dto.getBiometricEnabled());
        mobilePreferenceMapper.updateById(preference);
        return buildSecurityDto(preference);
    }

    @Override
    @Transactional
    public SecuritySettingsDto resetSecuritySettings(String authorization, String roleType) {
        UserPo user = resolveUser(authorization, roleType);
        MobilePreferencePo preference = requirePreference(user, roleType);
        applyDefaultSecurity(preference);
        mobilePreferenceMapper.updateById(preference);
        return buildSecurityDto(preference);
    }

    @Override
    @Transactional
    public SecuritySettingsDto checkPassword(String authorization, String roleType, PasswordCheckDto dto) {
        UserPo user = resolveUser(authorization, roleType);
        validatePassword(dto, user);

        MobilePreferencePo preference = requirePreference(user, roleType);
        preference.setPasswordCheckedAt(LocalDateTime.now());
        mobilePreferenceMapper.updateById(preference);
        return buildSecurityDto(preference);
    }

    private UserPo resolveUser(String authorization, String roleType) {
        String normalizedRole = text(roleType).toLowerCase();
        return CurrentUserSupport.requireUser(authorization, normalizedRole, userMapper);
        /* if (parsedToken != null && normalizedRole.equalsIgnoreCase(parsedToken.roleType())) {
            UserPo user = userMapper.selectById(parsedToken.userId());
            if (isActiveUser(user, normalizedRole)) {
                return user;
            }
        }

        if ("student".equals(normalizedRole)) {
            List<StudentPo> students = studentMapper.selectList(new LambdaQueryWrapper<StudentPo>()
                    .eq(StudentPo::getStatus, "enabled")
                    .orderByAsc(StudentPo::getId)
                    .last("limit 1"));
            for (StudentPo student : students) {
                UserPo user = userMapper.selectById(student.getStudentUserId());
                if (isActiveUser(user, normalizedRole)) {
                    return user;
                }
            }
        }

        if ("parent".equals(normalizedRole)) {
            List<ParentPo> parents = parentMapper.selectList(new LambdaQueryWrapper<ParentPo>()
                    .orderByAsc(ParentPo::getId)
                    .last("limit 1"));
            for (ParentPo parent : parents) {
                UserPo user = userMapper.selectById(parent.getParentUserId());
                if (isActiveUser(user, normalizedRole)) {
                    return user;
                }
            }
        }

        throw new CommonException(40101, "璇峰厛鐧诲綍");
    }

        */
    }

    private boolean isActiveUser(UserPo user, String roleType) {
        return CurrentUserSupport.isActiveUser(user, roleType);
    }

    private MobilePreferencePo requirePreference(UserPo user, String roleType) {
        MobilePreferencePo preference = mobilePreferenceMapper.selectOne(new LambdaQueryWrapper<MobilePreferencePo>()
                .eq(MobilePreferencePo::getUserId, user.getId())
                .eq(MobilePreferencePo::getRoleType, text(roleType).toLowerCase())
                .last("limit 1"));
        if (preference != null) {
            normalizePreference(preference);
            return preference;
        }

        MobilePreferencePo created = new MobilePreferencePo();
        created.setUserId(user.getId());
        created.setRoleType(text(roleType).toLowerCase());
        applyDefaultNotification(created);
        applyDefaultSecurity(created);
        mobilePreferenceMapper.insert(created);
        return created;
    }

    private void normalizePreference(MobilePreferencePo preference) {
        if (preference.getMasterEnabled() == null) {
            applyDefaultNotification(preference);
        }
        if (preference.getHideAccountIdentifier() == null) {
            applyDefaultSecurity(preference);
        }
    }

    private void applyDefaultNotification(MobilePreferencePo preference) {
        preference.setMasterEnabled(defaultBoolean(preference.getMasterEnabled(), true));
        preference.setAssignmentEnabled(defaultBoolean(preference.getAssignmentEnabled(), true));
        preference.setReviewEnabled(defaultBoolean(preference.getReviewEnabled(), true));
        preference.setReminderEnabled(defaultBoolean(preference.getReminderEnabled(), true));
        preference.setSystemEnabled(defaultBoolean(preference.getSystemEnabled(), true));
        preference.setSoundEnabled(defaultBoolean(preference.getSoundEnabled(), true));
        preference.setVibrationEnabled(defaultBoolean(preference.getVibrationEnabled(), true));
        preference.setQuietHoursEnabled(defaultBoolean(preference.getQuietHoursEnabled(), false));
        preference.setQuietStart(StringUtils.hasText(preference.getQuietStart()) ? preference.getQuietStart().trim() : "22:00");
        preference.setQuietEnd(StringUtils.hasText(preference.getQuietEnd()) ? preference.getQuietEnd().trim() : "07:00");
    }

    private void applyDefaultSecurity(MobilePreferencePo preference) {
        preference.setHideAccountIdentifier(defaultBoolean(preference.getHideAccountIdentifier(), false));
        preference.setRememberAccount(defaultBoolean(preference.getRememberAccount(), true));
        preference.setLoginAlertEnabled(defaultBoolean(preference.getLoginAlertEnabled(), true));
        preference.setAppLockEnabled(defaultBoolean(preference.getAppLockEnabled(), false));
        preference.setBiometricEnabled(defaultBoolean(preference.getBiometricEnabled(), false));
        if (preference.getId() == null) {
            preference.setPasswordCheckedAt(null);
        }
    }

    private NotificationSettingsDto buildNotificationDto(MobilePreferencePo preference) {
        NotificationSettingsDto dto = new NotificationSettingsDto();
        dto.setMasterEnabled(defaultBoolean(preference.getMasterEnabled(), true));
        dto.setAssignmentEnabled(defaultBoolean(preference.getAssignmentEnabled(), true));
        dto.setReviewEnabled(defaultBoolean(preference.getReviewEnabled(), true));
        dto.setReminderEnabled(defaultBoolean(preference.getReminderEnabled(), true));
        dto.setSystemEnabled(defaultBoolean(preference.getSystemEnabled(), true));
        dto.setSoundEnabled(defaultBoolean(preference.getSoundEnabled(), true));
        dto.setVibrationEnabled(defaultBoolean(preference.getVibrationEnabled(), true));
        dto.setQuietHoursEnabled(defaultBoolean(preference.getQuietHoursEnabled(), false));
        dto.setQuietStart(StringUtils.hasText(preference.getQuietStart()) ? preference.getQuietStart().trim() : "22:00");
        dto.setQuietEnd(StringUtils.hasText(preference.getQuietEnd()) ? preference.getQuietEnd().trim() : "07:00");
        return dto;
    }

    private SecuritySettingsDto buildSecurityDto(MobilePreferencePo preference) {
        SecuritySettingsDto dto = new SecuritySettingsDto();
        dto.setHideAccountIdentifier(defaultBoolean(preference.getHideAccountIdentifier(), false));
        dto.setRememberAccount(defaultBoolean(preference.getRememberAccount(), true));
        dto.setLoginAlertEnabled(defaultBoolean(preference.getLoginAlertEnabled(), true));
        dto.setAppLockEnabled(defaultBoolean(preference.getAppLockEnabled(), false));
        dto.setBiometricEnabled(defaultBoolean(preference.getBiometricEnabled(), false));
        dto.setPasswordCheckedAt(formatTime(preference.getPasswordCheckedAt()));
        return dto;
    }

    private void validatePassword(PasswordCheckDto dto, UserPo user) {
        String currentPassword = text(dto.getCurrentPassword());
        String nextPassword = text(dto.getNextPassword());
        String confirmPassword = text(dto.getConfirmPassword());

        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(nextPassword) || !StringUtils.hasText(confirmPassword)) {
            throw new CommonException("\u8bf7\u5b8c\u6574\u586b\u5199\u5bc6\u7801\u4fe1\u606f");
        }
        if (!PasswordSupport.matches(currentPassword, user.getPasswordHash())) {
            throw new CommonException("\u5f53\u524d\u5bc6\u7801\u4e0d\u6b63\u786e");
        }
        if (nextPassword.length() < 6) {
            throw new CommonException("\u65b0\u5bc6\u7801\u81f3\u5c116\u4f4d");
        }
        if (!nextPassword.matches(".*[A-Za-z].*") || !nextPassword.matches(".*\\d.*")) {
            throw new CommonException("\u65b0\u5bc6\u7801\u9700\u540c\u65f6\u5305\u542b\u5b57\u6bcd\u548c\u6570\u5b57");
        }
        if (currentPassword.equals(nextPassword)) {
            throw new CommonException("鏂板瘑鐮佷笉鑳戒笌褰撳墠瀵嗙爜鐩稿悓");
        }
        if (!nextPassword.equals(confirmPassword)) {
            throw new CommonException("\u4e24\u6b21\u8f93\u5165\u7684\u65b0\u5bc6\u7801\u4e0d\u4e00\u81f4");
        }
    }

    private boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : TIME_FORMATTER.format(time);
    }

    private String text(String text) {
        return StringUtils.hasText(text) ? text.trim() : "";
    }
}
