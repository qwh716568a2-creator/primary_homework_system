package com.primaryhomework.backend.entity.dto.admin;

import lombok.Data;

@Data
public class AdminUserQueryDto {

    private String keyword;
    private String roleType;
    private Long schoolId;
    private String status;
    private Integer pageNo;
    private Integer pageSize;
}
