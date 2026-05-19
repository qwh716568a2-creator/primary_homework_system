package com.primaryhomework.backend.entity.dto.teacher;

import lombok.Data;

@Data
public class HomeworkTaskQueryDto {

    private Long classId;
    private String taskStatus;
    private String keyword;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
