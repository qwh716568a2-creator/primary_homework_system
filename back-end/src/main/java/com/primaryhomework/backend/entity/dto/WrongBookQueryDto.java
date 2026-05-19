package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class WrongBookQueryDto {

    private String subjectCode;
    private String status;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
