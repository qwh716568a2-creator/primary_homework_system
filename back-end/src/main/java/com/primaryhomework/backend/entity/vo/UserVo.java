package com.primaryhomework.backend.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserVo {

    private Long userId;
    private String userName;
    private String roleType;
    private Long schoolId;
    private String schoolName;
    private List<String> permissions = new ArrayList<>();
}
