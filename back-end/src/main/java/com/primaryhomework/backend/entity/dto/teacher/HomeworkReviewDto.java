package com.primaryhomework.backend.entity.dto.teacher;

import com.primaryhomework.backend.entity.dto.WrongItemDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkReviewDto {

    @NotNull(message = "submissionId is required")
    private Long submissionId;

    @NotBlank(message = "reviewStatus is required")
    private String reviewStatus;

    private BigDecimal score;
    private String scoreLevel;
    private String commentText;
    private List<HomeworkReviewAssetDto> reviewAssets = new ArrayList<>();
    private List<WrongItemDto> wrongItems = new ArrayList<>();
}
