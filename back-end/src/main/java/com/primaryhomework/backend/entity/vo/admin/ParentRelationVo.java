package com.primaryhomework.backend.entity.vo.admin;

import lombok.Data;

@Data
public class ParentRelationVo {

    private Long id;
    private Long parentUserId;
    private String parentName;
    private String parentMobile;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private String relationType;
    private Boolean isPrimary;
    private String status;
}
