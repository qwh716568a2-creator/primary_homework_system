package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubmissionVo {

    private String id;
    private String operatorRole;
    private String text;
    private List<String> images = new ArrayList<>();
    private String submittedAt;
    private Boolean assistedByParent;
    private Integer versionNo;
}
