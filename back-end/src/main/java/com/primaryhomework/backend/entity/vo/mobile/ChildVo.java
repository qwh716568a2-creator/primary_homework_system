package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

@Data
public class ChildVo {

    private String id;
    private String name;
    private String className;
    private String gradeName;
    private Integer pendingCount;
    private Integer submittedCount;
    private Integer revisionCount;
}
