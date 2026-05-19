package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskDetailDto {

    private TaskInfoDto taskInfo;
    private List<SubmissionDto> submissions = new ArrayList<>();
    private List<ReviewItemDto> reviews = new ArrayList<>();
}
