package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WrongItemDto {

    private String questionNo;
    private String questionText;
    private String studentAnswer;
    private String correctAnswer;
    private String analysisText;
    private String wrongReasonCode;
    private List<WrongBookAssetDto> assets = new ArrayList<>();
}
