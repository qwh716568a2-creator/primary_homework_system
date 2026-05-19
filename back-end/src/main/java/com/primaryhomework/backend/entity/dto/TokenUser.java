package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class TokenUser {

    private Long userId;
    private String roleType;
}
