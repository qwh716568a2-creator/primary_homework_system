package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class StatsDto {

    private Integer publishCount;
    private Double submissionRate;
    private Double onTimeRate;
    private Double reviewRate;
    private Double revisionRequiredRate;
}
