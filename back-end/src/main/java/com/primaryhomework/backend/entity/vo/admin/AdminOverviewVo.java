package com.primaryhomework.backend.entity.vo.admin;

import lombok.Data;

@Data
public class AdminOverviewVo {

    private Integer publishCountToday;
    private Double submissionRate;
    private Double overdueRate;
    private Integer activeTeacherCount;
    private Integer activeStudentCount;
}
