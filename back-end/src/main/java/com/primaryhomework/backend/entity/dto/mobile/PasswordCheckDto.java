package com.primaryhomework.backend.entity.dto.mobile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordCheckDto {

    @NotBlank(message = "currentPassword\u4e0d\u80fd\u4e3a\u7a7a")
    private String currentPassword;

    @NotBlank(message = "nextPassword\u4e0d\u80fd\u4e3a\u7a7a")
    private String nextPassword;

    @NotBlank(message = "confirmPassword\u4e0d\u80fd\u4e3a\u7a7a")
    private String confirmPassword;
}
