package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

@Data
public class HomeworkOverviewStatsVo {

    private Integer publishCount;
    private Double submissionRate;
    private Double onTimeRate;
    private Double reviewRate;
    private Double revisionRequiredRate;
}
