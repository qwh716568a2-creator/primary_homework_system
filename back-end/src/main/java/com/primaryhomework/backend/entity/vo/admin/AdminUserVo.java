package com.primaryhomework.backend.entity.vo.admin;

import lombok.Data;

@Data
public class AdminUserVo {

    private Long userId;
    private String userName;
    private String roleType;
    private Long schoolId;
    private String schoolName;
    private String status;
    private String loginName;
    private String mobile;
    private String teacherNo;
    private String studentNo;
    private Long classId;
    private String className;
    private Long gradeId;
    private String gradeName;
    private String account;
}
