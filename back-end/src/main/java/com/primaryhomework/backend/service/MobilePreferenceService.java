package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.dto.mobile.NotificationSettingsDto;
import com.primaryhomework.backend.entity.dto.mobile.PasswordCheckDto;
import com.primaryhomework.backend.entity.dto.mobile.SecuritySettingsDto;

public interface MobilePreferenceService {

    NotificationSettingsDto getNotificationSettings(String authorization, String roleType);

    NotificationSettingsDto saveNotificationSettings(String authorization, String roleType, NotificationSettingsDto dto);

    NotificationSettingsDto resetNotificationSettings(String authorization, String roleType);

    void markAllRead(String authorization, String roleType);

    SecuritySettingsDto getSecuritySettings(String authorization, String roleType);

    SecuritySettingsDto saveSecuritySettings(String authorization, String roleType, SecuritySettingsDto dto);

    SecuritySettingsDto resetSecuritySettings(String authorization, String roleType);

    SecuritySettingsDto checkPassword(String authorization, String roleType, PasswordCheckDto dto);
}
