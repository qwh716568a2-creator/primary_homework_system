package com.primaryhomework.backend.entity.dto.mobile;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubmitDto {

    @Size(max = 2000, message = "\u63d0\u4ea4\u8bf4\u660e\u4e0d\u80fd\u8d85\u8fc72000\u5b57")
    private String text;

    private List<String> images = new ArrayList<>();

    private Long studentId;

    private Boolean assistedByParent;
}
