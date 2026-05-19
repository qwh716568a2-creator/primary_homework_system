package com.primaryhomework.backend.entity.dto.admin;

import lombok.Data;

@Data
public class AdminUserSaveDto {

    private String userName;
    private String roleType;
    private Long schoolId;
    private String password;
    private String loginName;
    private String status;
    private Profile profile;

    @Data
    public static class Profile {
        private String teacherNo;
        private String mobile;
        private String studentNo;
        private Long gradeId;
        private Long classId;
    }
}
