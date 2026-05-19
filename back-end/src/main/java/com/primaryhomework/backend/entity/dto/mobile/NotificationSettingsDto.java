package com.primaryhomework.backend.entity.dto.mobile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationSettingsDto {

    @NotNull(message = "masterEnabled不能为空")
    private Boolean masterEnabled;

    @NotNull(message = "assignmentEnabled不能为空")
    private Boolean assignmentEnabled;

    @NotNull(message = "reviewEnabled不能为空")
    private Boolean reviewEnabled;

    @NotNull(message = "reminderEnabled不能为空")
    private Boolean reminderEnabled;

    @NotNull(message = "systemEnabled不能为空")
    private Boolean systemEnabled;

    @NotNull(message = "soundEnabled不能为空")
    private Boolean soundEnabled;

    @NotNull(message = "vibrationEnabled不能为空")
    private Boolean vibrationEnabled;

    @NotNull(message = "quietHoursEnabled不能为空")
    private Boolean quietHoursEnabled;

    @NotBlank(message = "quietStart不能为空")
    private String quietStart;

    @NotBlank(message = "quietEnd不能为空")
    private String quietEnd;
}
