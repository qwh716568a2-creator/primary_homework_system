package com.primaryhomework.backend.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WrongBookFixDto {

    @NotBlank(message = "fixedText is required")
    private String fixedText;
    private List<WrongBookAssetDto> assets = new ArrayList<>();
}
