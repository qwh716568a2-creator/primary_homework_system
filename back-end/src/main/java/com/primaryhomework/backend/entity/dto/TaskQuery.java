package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class TaskQuery {

    private Long classId;
    private String taskStatus;
    private String keyword;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
