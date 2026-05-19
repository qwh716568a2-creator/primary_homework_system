package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkDetailDto {

    private HomeworkBaseDto baseInfo;
    private List<HomeworkClassDto> classList = new ArrayList<>();
    private List<AssetDto> attachments = new ArrayList<>();
}
