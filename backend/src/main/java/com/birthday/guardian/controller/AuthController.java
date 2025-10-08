package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.dto.LoginRequest;
import com.birthday.guardian.dto.LoginResponse;
import com.birthday.guardian.dto.RegisterRequest;
import com.birthday.guardian.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterRequest request) {
        try {
            System.out.println("收到注册请求: " + request.getUsername());
            userService.register(request);
            System.out.println("注册成功: " + request.getUsername());
            return Result.success();
        } catch (Exception e) {
            System.err.println("注册失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        try {
            System.out.println("收到登录请求: " + request.getAccount());
            LoginResponse response = userService.login(request);
            System.out.println("登录成功，返回Token");
            return Result.success(response);
        } catch (Exception e) {
            System.err.println("登录失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}
