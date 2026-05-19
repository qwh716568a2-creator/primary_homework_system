package com.primaryhomework.backend.entity.dto.teacher;

import lombok.Data;

@Data
public class HomeworkAssetDto {

    private String assetType;
    private String assetUrl;
    private String assetName;
    private Long assetSize;
}
