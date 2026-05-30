package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WrongBookPracticeItemVo {

    private Long practiceItemId;
    private Long wrongBookId;
    private String questionNo;
    private String subjectCode;
    private String subjectName;
    private String questionText;
    private String correctAnswer;
    private String studentAnswer;
    private String itemSourceType;
    private BigDecimal itemWeight;
    private String resultStatus;
    private Integer usedDurationSeconds;
    private Integer sortNo;
    private String submittedAt;
}
