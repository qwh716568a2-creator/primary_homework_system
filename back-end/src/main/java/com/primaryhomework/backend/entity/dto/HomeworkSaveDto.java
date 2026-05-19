package com.primaryhomework.backend.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkSaveDto {

    @NotBlank(message = "作业标题不能为空")
    private String title;

    @NotBlank(message = "学科不能为空")
    private String subjectCode;

    private String contentText;

    @NotEmpty(message = "班级不能为空")
    private List<Long> classIds = new ArrayList<>();

    @NotBlank(message = "截止时间不能为空")
    private String deadlineAt;

    private Boolean allowLateSubmit;
    private Boolean allowResubmit;
    private Boolean needParentConfirm;
    private List<String> submitTypes = new ArrayList<>();
    private List<AssetDto> attachments = new ArrayList<>();
    private Boolean publishNow;
}
