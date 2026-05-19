package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewItemDto {

    private Long reviewId;
    private String reviewStatus;
    private BigDecimal score;
    private String scoreLevel;
    private String commentText;
    private LocalDateTime reviewedAt;
    private List<AssetDto> reviewAssets = new ArrayList<>();
}
