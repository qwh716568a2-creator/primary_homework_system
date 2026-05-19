package com.primaryhomework.backend.utils;

import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.mapper.UserMapper;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;

public final class CurrentUserSupport {

    private CurrentUserSupport() {
    }

    public static UserPo requireUser(String authorization, String roleType, UserMapper userMapper) {
        String normalizedRole = text(roleType).toLowerCase(Locale.ROOT);
        TokenSupport.ParsedToken parsedToken = TokenSupport.parseAuthorization(authorization);
        if (parsedToken == null) {
            throw unauthorized(normalizedRole);
        }
        if (!Objects.equals(normalizedRole, text(parsedToken.roleType()).toLowerCase(Locale.ROOT))) {
            throw unauthorized(normalizedRole);
        }

        UserPo user = userMapper.selectById(parsedToken.userId());
        if (!isActiveUser(user, normalizedRole)) {
            throw unauthorized(normalizedRole);
        }
        return user;
    }

    public static boolean isActiveUser(UserPo user, String roleType) {
        return user != null
                && text(roleType).equalsIgnoreCase(text(user.getRoleType()))
                && (!StringUtils.hasText(user.getStatus()) || "enabled".equalsIgnoreCase(user.getStatus()));
    }

    private static CommonException unauthorized(String roleType) {
        return new CommonException(40101, "请先以" + roleName(roleType) + "身份登录");
    }

    private static String roleName(String roleType) {
        return switch (text(roleType).toLowerCase(Locale.ROOT)) {
            case "admin" -> "管理员";
            case "teacher" -> "教师";
            case "student" -> "学生";
            case "parent" -> "家长";
            default -> "当前用户";
        };
    }

    private static String text(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
