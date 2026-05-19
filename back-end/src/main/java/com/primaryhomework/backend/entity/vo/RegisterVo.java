package com.primaryhomework.backend.entity.vo;

import lombok.Data;

@Data
public class RegisterVo {

    private Long userId;
    private String account;
    private String userName;
    private String roleType;
    private Long schoolId;
    private String schoolName;
}
