package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class StatsQuery {

    private Long classId;
    private String subjectCode;
    private String startDate;
    private String endDate;
}
