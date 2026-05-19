package com.primaryhomework.backend.service.impl;

import com.primaryhomework.backend.entity.dto.mobile.PasswordCheckDto;
import com.primaryhomework.backend.entity.po.MobilePreferencePo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.dto.mobile.SecuritySettingsDto;
import com.primaryhomework.backend.mapper.MobilePreferenceMapper;
import com.primaryhomework.backend.mapper.NotificationMapper;
import com.primaryhomework.backend.mapper.ParentMapper;
import com.primaryhomework.backend.mapper.StudentMapper;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.utils.PasswordSupport;
import com.primaryhomework.backend.utils.TokenSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobilePreferenceServiceImplTest {

    @Mock
    private MobilePreferenceMapper mobilePreferenceMapper;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private ParentMapper parentMapper;

    @InjectMocks
    private MobilePreferenceServiceImpl service;

    @Test
    void checkPasswordShouldValidateOnlyAndNotUpdateUserPassword() {
        String oldPasswordHash = PasswordSupport.encode("abc123");

        UserPo user = new UserPo();
        user.setId(1001L);
        user.setRoleType("student");
        user.setStatus("enabled");
        user.setPasswordHash(oldPasswordHash);

        MobilePreferencePo preference = new MobilePreferencePo();
        preference.setId(2001L);
        preference.setUserId(1001L);
        preference.setRoleType("student");
        preference.setHideAccountIdentifier(false);
        preference.setRememberAccount(true);
        preference.setLoginAlertEnabled(true);
        preference.setAppLockEnabled(false);
        preference.setBiometricEnabled(false);

        PasswordCheckDto dto = new PasswordCheckDto();
        dto.setCurrentPassword("abc123");
        dto.setNextPassword("abc124");
        dto.setConfirmPassword("abc124");

        when(userMapper.selectById(1001L)).thenReturn(user);
        when(mobilePreferenceMapper.selectOne(any())).thenReturn(preference);

        String token = TokenSupport.buildToken(user);
        SecuritySettingsDto result = service.checkPassword("Bearer " + token, "student", dto);

        assertEquals(oldPasswordHash, user.getPasswordHash());
        assertFalse(result.getPasswordCheckedAt() == null || result.getPasswordCheckedAt().isBlank());
        assertFalse(result.getHideAccountIdentifier());
        verify(userMapper, never()).updateById((UserPo) any());
        verify(mobilePreferenceMapper).updateById(preference);
    }
}
