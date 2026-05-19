package com.primaryhomework.backend.entity.dto.mobile;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SecuritySettingsDto {

    @NotNull(message = "hideAccountIdentifier不能为空")
    private Boolean hideAccountIdentifier;

    @NotNull(message = "rememberAccount不能为空")
    private Boolean rememberAccount;

    @NotNull(message = "loginAlertEnabled不能为空")
    private Boolean loginAlertEnabled;

    @NotNull(message = "appLockEnabled不能为空")
    private Boolean appLockEnabled;

    @NotNull(message = "biometricEnabled不能为空")
    private Boolean biometricEnabled;

    private String passwordCheckedAt;
}
