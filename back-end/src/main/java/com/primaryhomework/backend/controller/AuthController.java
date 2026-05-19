package com.primaryhomework.backend.controller;

import com.primaryhomework.backend.entity.dto.LoginDto;
import com.primaryhomework.backend.entity.dto.RegisterDto;
import com.primaryhomework.backend.entity.vo.LoginVo;
import com.primaryhomework.backend.entity.vo.R;
import com.primaryhomework.backend.entity.vo.RegisterVo;
import com.primaryhomework.backend.entity.vo.UserVo;
import com.primaryhomework.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginVo> login(@Valid @RequestBody LoginDto loginDto) {
        return R.ok(authService.login(loginDto));
    }

    @PostMapping("/register")
    public R<RegisterVo> register(@Valid @RequestBody RegisterDto registerDto) {
        return R.ok(authService.register(registerDto));
    }

    @GetMapping("/me")
    public R<UserVo> currentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return R.ok(authService.currentUser(authorization));
    }
}
