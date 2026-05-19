package com.primaryhomework.backend.entity.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherRelationSaveDto {

    @NotNull(message = "teacherId\u4e0d\u80fd\u4e3a\u7a7a")
    private Long teacherId;

    @NotNull(message = "classId\u4e0d\u80fd\u4e3a\u7a7a")
    private Long classId;

    @NotBlank(message = "subjectCode\u4e0d\u80fd\u4e3a\u7a7a")
    private String subjectCode;

    private Boolean isHeadTeacher;
}