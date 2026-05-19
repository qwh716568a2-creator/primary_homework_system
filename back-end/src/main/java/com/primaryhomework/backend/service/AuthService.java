package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.dto.LoginDto;
import com.primaryhomework.backend.entity.dto.RegisterDto;
import com.primaryhomework.backend.entity.vo.LoginVo;
import com.primaryhomework.backend.entity.vo.RegisterVo;
import com.primaryhomework.backend.entity.vo.UserVo;

public interface AuthService {

    LoginVo login(LoginDto loginDto);

    RegisterVo register(RegisterDto registerDto);

    UserVo currentUser(String authorization);
}
