package com.primaryhomework.backend.entity.dto.admin;

import lombok.Data;

@Data
public class AdminClassQueryDto {

    private Long schoolId;
    private Long gradeId;
    private String keyword;
}
