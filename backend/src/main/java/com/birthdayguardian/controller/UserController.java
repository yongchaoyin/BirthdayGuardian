package com.birthdayguardian.controller;

import com.birthdayguardian.common.Result;
import com.birthdayguardian.entity.User;
import com.birthdayguardian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 查询用户信息
     */
    @GetMapping("/profile")
    public Result<User> getProfile(@RequestParam Long userId) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 不返回密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 设置密码
     */
    @PostMapping("/setPassword")
    public Result<Object> setPassword(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String password = (String) request.get("password");
        
        if (password == null || password.trim().isEmpty()) {
            return Result.badRequest("密码不能为空");
        }
        
        if (password.length() < 6) {
            return Result.badRequest("密码长度不能少于6位");
        }
        
        try {
            userService.setPassword(userId, password);
            return Result.success("密码设置成功");
        } catch (Exception e) {
            return Result.error("密码设置失败：" + e.getMessage());
        }
    }
}