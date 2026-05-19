package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class HomeworkQuery {

    private String keyword;
    private Long classId;
    private String subjectCode;
    private String status;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
