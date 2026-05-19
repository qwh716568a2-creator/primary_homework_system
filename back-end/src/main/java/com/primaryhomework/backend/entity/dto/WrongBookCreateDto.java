package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WrongBookCreateDto {

    private String subjectCode;
    private String subjectName;
    private Long homeworkId;
    private Long taskId;
    private Long submissionId;
    private Long reviewId;
    private String questionNo;
    private String questionText;
    private String studentAnswer;
    private String correctAnswer;
    private String analysisText;
    private String wrongReasonCode;
    private List<WrongBookAssetDto> assets = new ArrayList<>();
}
