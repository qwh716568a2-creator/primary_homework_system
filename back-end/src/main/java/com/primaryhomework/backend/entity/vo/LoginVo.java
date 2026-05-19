package com.primaryhomework.backend.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LoginVo {

    private String role;
    private String userId;
    private String userName;
    private String schoolName;
    private String token;
    private Integer expiresIn;
    private UserVo userInfo;
    private List<String> permissions = new ArrayList<>();
}
