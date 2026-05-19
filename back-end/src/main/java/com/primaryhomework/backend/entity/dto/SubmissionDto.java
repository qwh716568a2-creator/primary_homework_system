package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SubmissionDto {

    private Long submissionId;
    private Integer versionNo;
    private String operatorRole;
    private LocalDateTime submittedAt;
    private String submitText;
    private List<AssetDto> assets = new ArrayList<>();
}
