package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class AssetDto {

    private String assetType;
    private String assetUrl;
    private String assetName;
    private Long assetSize;
}
