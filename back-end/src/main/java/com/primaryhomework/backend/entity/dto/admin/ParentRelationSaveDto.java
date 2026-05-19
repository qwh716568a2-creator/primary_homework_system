package com.primaryhomework.backend.entity.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParentRelationSaveDto {

    @NotNull(message = "parentUserId不能为空")
    private Long parentUserId;

    @NotNull(message = "studentId不能为空")
    private Long studentId;

    @NotBlank(message = "relationType不能为空")
    private String relationType;

    private Boolean isPrimary;
}
