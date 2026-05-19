package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mobile_preference")
public class MobilePreferencePo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String roleType;

    private Boolean masterEnabled;
    private Boolean assignmentEnabled;
    private Boolean reviewEnabled;
    private Boolean reminderEnabled;
    private Boolean systemEnabled;
    private Boolean soundEnabled;
    private Boolean vibrationEnabled;
    private Boolean quietHoursEnabled;
    private String quietStart;
    private String quietEnd;

    private Boolean hideAccountIdentifier;
    private Boolean rememberAccount;
    private Boolean loginAlertEnabled;
    private Boolean appLockEnabled;
    private Boolean biometricEnabled;
    private LocalDateTime passwordCheckedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
