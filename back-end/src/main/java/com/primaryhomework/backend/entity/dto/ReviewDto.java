package com.primaryhomework.backend.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewDto {

    @NotNull(message = "submissionId 不能为空")
    private Long submissionId;

    @NotBlank(message = "reviewStatus 不能为空")
    private String reviewStatus;

    private BigDecimal score;
    private String scoreLevel;
    private String commentText;
    private List<AssetDto> reviewAssets = new ArrayList<>();
}
