package com.primaryhomework.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.primaryhomework.backend.entity.dto.LoginDto;
import com.primaryhomework.backend.entity.dto.RegisterDto;
import com.primaryhomework.backend.entity.po.ParentPo;
import com.primaryhomework.backend.entity.po.SchoolPo;
import com.primaryhomework.backend.entity.po.StudentPo;
import com.primaryhomework.backend.entity.po.TeacherPo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.vo.LoginVo;
import com.primaryhomework.backend.entity.vo.RegisterVo;
import com.primaryhomework.backend.entity.vo.UserVo;
import com.primaryhomework.backend.mapper.ParentMapper;
import com.primaryhomework.backend.mapper.SchoolMapper;
import com.primaryhomework.backend.mapper.StudentMapper;
import com.primaryhomework.backend.mapper.TeacherMapper;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.service.AuthService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.PasswordSupport;
import com.primaryhomework.backend.utils.TokenSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${app.jwt.expire-seconds:7200}")
    private Integer expireSeconds;

    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final ParentMapper parentMapper;
    private final StudentMapper studentMapper;
    private final SchoolMapper schoolMapper;

    @Override
    public LoginVo login(LoginDto loginDto) {
        String loginType = text(loginDto.getLoginType()).toLowerCase();
        String account = text(loginDto.getAccount());
        String password = text(loginDto.getPassword());
        Long schoolId = loginDto.getSchoolId();

        if ("student".equals(loginType) && schoolId == null) {
            throw new CommonException("学生登录必须传 schoolId");
        }

        UserPo user = null;
        Long userSchoolId = schoolId;

        if ("admin".equals(loginType)) {
            user = findAdmin(account, schoolId);
            if (user != null) {
                userSchoolId = user.getSchoolId();
            }
        } else if ("teacher".equals(loginType)) {
            TeacherPo teacher = findTeacher(account, schoolId);
            if (teacher != null) {
                user = findUser(teacher.getTeacherUserId(), "teacher");
                userSchoolId = teacher.getSchoolId();
            }
        } else if ("parent".equals(loginType)) {
            ParentPo parent = findParent(account, schoolId);
            if (parent != null) {
                user = findUser(parent.getParentUserId(), "parent");
                userSchoolId = parent.getSchoolId();
            }
        } else if ("student".equals(loginType)) {
            StudentPo student = findStudent(account, schoolId);
            if (student != null) {
                user = findUser(student.getStudentUserId(), "student");
                userSchoolId = student.getSchoolId();
            }
        } else {
            throw new CommonException("登录类型错误");
        }

        if (user == null) {
            throw new CommonException("账号或密码错误");
        }
        if (StringUtils.hasText(user.getStatus()) && !"enabled".equalsIgnoreCase(user.getStatus())) {
            throw new CommonException("账号已被禁用");
        }
        if (!PasswordSupport.matches(password, user.getPasswordHash())) {
            throw new CommonException("账号或密码错误");
        }

        if (PasswordSupport.needsUpgrade(user.getPasswordHash())) {
            user.setPasswordHash(PasswordSupport.encode(password));
        }
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        LoginVo loginVo = new LoginVo();
        UserVo userVo = buildUserVo(user, userSchoolId);
        loginVo.setRole(userVo.getRoleType());
        loginVo.setUserId(userVo.getUserId() == null ? null : String.valueOf(userVo.getUserId()));
        loginVo.setUserName(userVo.getUserName());
        loginVo.setSchoolName(userVo.getSchoolName());
        loginVo.setToken(TokenSupport.buildToken(user));
        loginVo.setExpiresIn(expireSeconds);
        loginVo.setUserInfo(userVo);
        loginVo.setPermissions(userVo.getPermissions());
        return loginVo;
    }

    @Override
    @Transactional
    public RegisterVo register(RegisterDto registerDto) {
        String name = text(registerDto.getName());
        String schoolName = text(registerDto.getSchool());
        String role = text(registerDto.getRole()).toLowerCase();
        String account = text(registerDto.getAccount());
        String password = text(registerDto.getPassword());
        String confirmPassword = text(registerDto.getConfirmPassword());

        if (!password.equals(confirmPassword)) {
            throw new CommonException("两次输入的密码不一致");
        }

        SchoolPo school = schoolMapper.selectOne(
                new LambdaQueryWrapper<SchoolPo>()
                        .eq(SchoolPo::getSchoolName, schoolName)
                        .eq(SchoolPo::getStatus, "enabled")
                        .last("limit 1")
        );
        if (school == null) {
            throw new CommonException("学校名称不正确");
        }

        UserPo user;
        if ("student".equals(role)) {
            user = findStudentRegisterUser(school.getId(), account);
        } else if ("parent".equals(role)) {
            user = findParentRegisterUser(school.getId(), account);
        } else {
            throw new CommonException("当前只支持学生和家长注册");
        }

        if (!name.equals(text(user.getUserName()))) {
            throw new CommonException("姓名和档案信息不一致");
        }

        user.setPasswordHash(PasswordSupport.encode(password));
        userMapper.updateById(user);

        RegisterVo registerVo = new RegisterVo();
        registerVo.setUserId(user.getId());
        registerVo.setAccount(account);
        registerVo.setUserName(user.getUserName());
        registerVo.setRoleType(user.getRoleType());
        registerVo.setSchoolId(user.getSchoolId());
        registerVo.setSchoolName(school.getSchoolName());
        return registerVo;
    }

    @Override
    public UserVo currentUser(String authorization) {
        TokenSupport.ParsedToken parsedToken = TokenSupport.parseAuthorization(authorization);
        if (parsedToken == null) {
            throw new CommonException(40101, "未登录");
        }

        UserPo user = userMapper.selectById(parsedToken.userId());
        if (user == null) {
            throw new CommonException(40101, "用户不存在");
        }
        if (!parsedToken.roleType().equalsIgnoreCase(user.getRoleType())) {
            throw new CommonException(40301, "登录身份不匹配");
        }
        return buildUserVo(user, user.getSchoolId());
    }

    private UserPo findAdmin(String account, Long schoolId) {
        LambdaQueryWrapper<UserPo> wrapper = new LambdaQueryWrapper<UserPo>()
                .eq(UserPo::getLoginName, account)
                .eq(UserPo::getRoleType, "admin")
                .last("limit 1");
        if (schoolId != null) {
            wrapper.eq(UserPo::getSchoolId, schoolId);
        }
        return userMapper.selectOne(wrapper);
    }

    private TeacherPo findTeacher(String mobile, Long schoolId) {
        LambdaQueryWrapper<TeacherPo> wrapper = new LambdaQueryWrapper<TeacherPo>()
                .eq(TeacherPo::getMobile, mobile)
                .last("limit 1");
        if (schoolId != null) {
            wrapper.eq(TeacherPo::getSchoolId, schoolId);
        }
        return teacherMapper.selectOne(wrapper);
    }

    private ParentPo findParent(String mobile, Long schoolId) {
        LambdaQueryWrapper<ParentPo> wrapper = new LambdaQueryWrapper<ParentPo>()
                .eq(ParentPo::getMobile, mobile)
                .last("limit 1");
        if (schoolId != null) {
            wrapper.eq(ParentPo::getSchoolId, schoolId);
        }
        return parentMapper.selectOne(wrapper);
    }

    private StudentPo findStudent(String studentNo, Long schoolId) {
        List<StudentPo> list = studentMapper.selectList(
                new LambdaQueryWrapper<StudentPo>()
                        .eq(StudentPo::getStudentNo, studentNo)
                        .eq(StudentPo::getSchoolId, schoolId)
        );
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new CommonException("该学号匹配到多个学生");
        }
        return list.get(0);
    }

    private UserPo findStudentRegisterUser(Long schoolId, String studentNo) {
        StudentPo student = findStudent(studentNo, schoolId);
        if (student == null) {
            throw new CommonException("没有找到对应的学生档案");
        }
        UserPo user = findUser(student.getStudentUserId(), "student");
        if (user == null) {
            throw new CommonException("学生账号不存在");
        }
        return user;
    }

    private UserPo findParentRegisterUser(Long schoolId, String mobile) {
        ParentPo parent = parentMapper.selectOne(
                new LambdaQueryWrapper<ParentPo>()
                        .eq(ParentPo::getSchoolId, schoolId)
                        .eq(ParentPo::getMobile, mobile)
                        .last("limit 1")
        );
        if (parent == null) {
            throw new CommonException("没有找到对应的家长档案");
        }
        UserPo user = findUser(parent.getParentUserId(), "parent");
        if (user == null) {
            throw new CommonException("家长账号不存在");
        }
        return user;
    }

    private UserPo findUser(Long userId, String roleType) {
        UserPo user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        if (!roleType.equalsIgnoreCase(user.getRoleType())) {
            return null;
        }
        return user;
    }

    private String getSchoolName(Long schoolId) {
        if (schoolId == null) {
            return "";
        }
        SchoolPo school = schoolMapper.selectById(schoolId);
        if (school == null) {
            return "";
        }
        return school.getSchoolName();
    }

    private UserVo buildUserVo(UserPo user, Long schoolId) {
        UserVo userVo = new UserVo();
        userVo.setUserId(user.getId());
        userVo.setUserName(user.getUserName());
        userVo.setRoleType(user.getRoleType());
        userVo.setSchoolId(schoolId);
        userVo.setSchoolName(getSchoolName(schoolId));
        userVo.setPermissions(resolvePermissions(user.getRoleType()));
        return userVo;
    }

    private List<String> resolvePermissions(String roleType) {
        String normalizedRole = text(roleType).toLowerCase();
        if ("teacher".equals(normalizedRole)) {
            return List.of(
                    "teacher.homework.list",
                    "teacher.homework.publish",
                    "teacher.homework.detail",
                    "teacher.homework.review",
                    "teacher.homework.stats"
            );
        }
        if ("admin".equals(normalizedRole)) {
            return List.of(
                    "admin.dashboard",
                    "admin.schools.view",
                    "admin.classes.view",
                    "admin.users.manage",
                    "admin.teacher-relations.manage",
                    "admin.parent-relations.manage"
            );
        }
        if ("student".equals(normalizedRole)) {
            return List.of("student.homework.list", "student.homework.submit");
        }
        if ("parent".equals(normalizedRole)) {
            return List.of("parent.student.list", "parent.homework.list");
        }
        return List.of();
    }

    private String text(String text) {
        return StringUtils.hasText(text) ? text.trim() : "";
    }
}
