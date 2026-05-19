package com.primaryhomework.backend.service.impl;

import com.primaryhomework.backend.entity.dto.LoginDto;
import com.primaryhomework.backend.entity.dto.RegisterDto;
import com.primaryhomework.backend.entity.po.SchoolPo;
import com.primaryhomework.backend.entity.po.StudentPo;
import com.primaryhomework.backend.entity.po.TeacherPo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.vo.LoginVo;
import com.primaryhomework.backend.entity.vo.RegisterVo;
import com.primaryhomework.backend.mapper.ParentMapper;
import com.primaryhomework.backend.mapper.SchoolMapper;
import com.primaryhomework.backend.mapper.StudentMapper;
import com.primaryhomework.backend.mapper.TeacherMapper;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.utils.PasswordSupport;
import com.primaryhomework.backend.utils.TokenSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private ParentMapper parentMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private SchoolMapper schoolMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expireSeconds", 7200);
    }

    @Test
    void loginShouldUpgradeLegacyPlainTextPassword() {
        LoginDto loginDto = new LoginDto();
        loginDto.setLoginType("teacher");
        loginDto.setAccount("13900000002");
        loginDto.setPassword("123456");
        loginDto.setSchoolId(1L);

        TeacherPo teacher = new TeacherPo();
        teacher.setTeacherUserId(3001L);
        teacher.setSchoolId(1L);
        teacher.setMobile("13900000002");

        UserPo user = new UserPo();
        user.setId(3001L);
        user.setRoleType("teacher");
        user.setUserName("张老师");
        user.setSchoolId(1L);
        user.setStatus("enabled");
        user.setPasswordHash("123456");

        SchoolPo school = new SchoolPo();
        school.setId(1L);
        school.setSchoolName("示例小学");

        when(teacherMapper.selectOne(any())).thenReturn(teacher);
        when(userMapper.selectById(3001L)).thenReturn(user);
        when(schoolMapper.selectById(1L)).thenReturn(school);

        LoginVo result = authService.login(loginDto);

        assertEquals("teacher", result.getRole());
        assertNotNull(result.getToken());
        assertNotNull(TokenSupport.parseToken(result.getToken()));
        assertTrue(PasswordSupport.isEncoded(user.getPasswordHash()));
        assertTrue(PasswordSupport.matches("123456", user.getPasswordHash()));
        verify(userMapper).updateById(user);
    }

    @Test
    void registerShouldStoreEncodedPassword() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setName("小明");
        registerDto.setSchool("示例小学");
        registerDto.setRole("student");
        registerDto.setAccount("S001");
        registerDto.setPassword("abc123");
        registerDto.setConfirmPassword("abc123");

        SchoolPo school = new SchoolPo();
        school.setId(1L);
        school.setSchoolName("示例小学");
        school.setStatus("enabled");

        StudentPo student = new StudentPo();
        student.setStudentUserId(4001L);
        student.setSchoolId(1L);
        student.setStudentNo("S001");

        UserPo user = new UserPo();
        user.setId(4001L);
        user.setRoleType("student");
        user.setUserName("小明");
        user.setSchoolId(1L);

        when(schoolMapper.selectOne(any())).thenReturn(school);
        when(studentMapper.selectList(any())).thenReturn(List.of(student));
        when(userMapper.selectById(4001L)).thenReturn(user);

        RegisterVo result = authService.register(registerDto);

        assertEquals(4001L, result.getUserId());
        assertTrue(PasswordSupport.isEncoded(user.getPasswordHash()));
        assertTrue(PasswordSupport.matches("abc123", user.getPasswordHash()));
        verify(userMapper).updateById(user);
    }
}
