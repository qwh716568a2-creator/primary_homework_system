package com.primaryhomework.backend.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WrongBookPracticeSubmitDto {

    @NotNull(message = "practiceId cannot be null")
    private Long practiceId;

    @Valid
    private List<WrongBookPracticeSubmitItemDto> items = new ArrayList<>();
}
