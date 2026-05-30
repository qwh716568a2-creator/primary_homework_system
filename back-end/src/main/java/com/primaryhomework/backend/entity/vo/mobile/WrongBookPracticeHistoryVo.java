package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WrongBookPracticeHistoryVo {

    private Long practiceId;
    private String practiceName;
    private String practiceType;
    private Integer questionCount;
    private Integer submittedCount;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracyRate;
    private String status;
    private String generatedAt;
    private String submittedAt;
}
