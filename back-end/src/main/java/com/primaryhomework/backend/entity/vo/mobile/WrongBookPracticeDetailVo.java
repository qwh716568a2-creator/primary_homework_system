package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class WrongBookPracticeDetailVo {

    private Long practiceId;
    private String practiceName;
    private String practiceType;
    private Integer questionCount;
    private Integer wrongQuestionCount;
    private Integer riskyQuestionCount;
    private Integer submittedCount;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracyRate;
    private String status;
    private String generatedAt;
    private String submittedAt;
    private List<WrongBookPracticeItemVo> items = new ArrayList<>();
}
