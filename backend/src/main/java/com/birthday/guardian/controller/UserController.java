package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.dto.ChangePasswordRequest;
import com.birthday.guardian.dto.UpdateEmailRequest;
import com.birthday.guardian.dto.UpdatePhoneRequest;
import com.birthday.guardian.dto.UpdateUsernameRequest;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 不返回密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@Validated @RequestBody ChangePasswordRequest request,
                                       HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            System.out.println("修改密码请求 - 用户ID: " + userId);
            userService.changePassword(userId, request);
            System.out.println("密码修改成功 - 用户ID: " + userId);
            return Result.success();
        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update-email")
    public Result<Void> updateEmail(@Validated @RequestBody UpdateEmailRequest request,
                                    HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            System.out.println("修改邮箱请求 - 用户ID: " + userId);
            userService.updateEmail(userId, request);
            System.out.println("邮箱修改成功 - 用户ID: " + userId);
            return Result.success();
        } catch (Exception e) {
            System.err.println("修改邮箱失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update-phone")
    public Result<Void> updatePhone(@Validated @RequestBody UpdatePhoneRequest request,
                                    HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            userService.updatePhone(userId, request.getPhone());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update-username")
    public Result<Void> updateUsername(@Validated @RequestBody UpdateUsernameRequest request,
                                       HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            userService.updateUsername(userId, request.getUsername());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
