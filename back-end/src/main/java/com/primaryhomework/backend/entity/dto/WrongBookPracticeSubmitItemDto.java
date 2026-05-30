package com.primaryhomework.backend.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WrongBookPracticeSubmitItemDto {

    @NotNull(message = "practiceItemId cannot be null")
    private Long practiceItemId;
    private Long wrongBookId;
    private String studentAnswer;
    private String resultStatus;
    private Integer usedDurationSeconds;
}
