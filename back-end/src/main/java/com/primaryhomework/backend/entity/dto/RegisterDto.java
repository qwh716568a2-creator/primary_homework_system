package com.primaryhomework.backend.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDto {

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "学校名称不能为空")
    private String school;

    @NotBlank(message = "角色不能为空")
    private String role;

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}