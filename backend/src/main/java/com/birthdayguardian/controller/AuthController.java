package com.birthdayguardian.controller;

import com.birthdayguardian.common.Result;
import com.birthdayguardian.entity.User;
import com.birthdayguardian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendCode")
    public Result<Void> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return Result.badRequest("邮箱不能为空");
        }
        
        try {
            userService.sendVerificationCode(email);
            return Result.success();
        } catch (Exception e) {
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }

    /**
     * 邮箱验证码注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String nickname = request.get("nickname");
        
        if (email == null || email.trim().isEmpty()) {
            return Result.badRequest("邮箱不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.badRequest("验证码不能为空");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            return Result.badRequest("昵称不能为空");
        }
        
        try {
            User user = userService.register(email, code, nickname);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 登录（支持验证码和密码两种方式）
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String password = request.get("password");
        
        if (email == null || email.trim().isEmpty()) {
            return Result.badRequest("邮箱不能为空");
        }
        
        try {
            User user;
            if (code != null && !code.trim().isEmpty()) {
                // 验证码登录
                user = userService.loginByCode(email, code);
            } else if (password != null && !password.trim().isEmpty()) {
                // 密码登录
                user = userService.loginByPassword(email, password);
            } else {
                return Result.badRequest("请提供验证码或密码");
            }
            
            return Result.success("登录成功", user);
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }
}