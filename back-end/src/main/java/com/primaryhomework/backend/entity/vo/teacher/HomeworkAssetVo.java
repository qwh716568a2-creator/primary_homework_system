package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

@Data
public class HomeworkAssetVo {

    private String assetType;
    private String assetUrl;
    private String assetName;
    private Long assetSize;
}
