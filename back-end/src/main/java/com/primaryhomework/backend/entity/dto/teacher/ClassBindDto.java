package com.primaryhomework.backend.entity.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassBindDto {

    @NotNull(message = "classId不能为空")
    private Long classId;

    @NotBlank(message = "subjectCode不能为空")
    private String subjectCode;

    private Boolean isHeadTeacher;
}