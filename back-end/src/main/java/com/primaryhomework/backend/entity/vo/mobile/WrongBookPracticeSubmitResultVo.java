package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WrongBookPracticeSubmitResultVo {

    private Long practiceId;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracyRate;
    private Integer masteredCount;
    private Integer returnedToActiveCount;
}
