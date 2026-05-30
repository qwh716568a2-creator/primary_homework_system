package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WrongBookDetailVo {

    private Long id;
    private Long wrongBookId;
    private Long homeworkId;
    private Long taskId;
    private Long reviewId;
    private String subjectCode;
    private String subjectName;
    private String sourceType;
    private String questionNo;
    private String questionText;
    private String studentAnswer;
    private String correctAnswer;
    private String analysisText;
    private String wrongReasonCode;
    private String wrongReasonLabel;
    private String teacherName;
    private String status;
    private String poolType;
    private Integer correctStreak;
    private Integer practiceCount;
    private String lastPracticeResult;
    private String lastPracticedAt;
    private String lastFixedText;
    private String lastFixedAt;
    private Integer fixCount;
    private List<WrongBookAssetVo> assets = new ArrayList<>();
}
