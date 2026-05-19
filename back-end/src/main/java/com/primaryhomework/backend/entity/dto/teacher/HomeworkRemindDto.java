package com.primaryhomework.backend.entity.dto.teacher;

import lombok.Data;

@Data
public class HomeworkRemindDto {

    private String remindType;
    private Long classId;
}
