package com.birthday.guardian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.birthday.guardian.dto.ChangePasswordRequest;
import com.birthday.guardian.dto.LoginRequest;
import com.birthday.guardian.dto.LoginResponse;
import com.birthday.guardian.dto.RegisterRequest;
import com.birthday.guardian.dto.UpdateEmailRequest;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.mapper.UserMapper;
import com.birthday.guardian.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()));
        user.setRole("user");
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest request) {
        System.out.println("登录请求 - 账号: " + request.getAccount());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getUsername, request.getAccount())
                .or()
                .eq(User::getEmail, request.getAccount()));

        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            System.out.println("登录失败 - 用户不存在: " + request.getAccount());
            throw new RuntimeException("用户不存在");
        }

        System.out.println("找到用户 - ID: " + user.getId() + ", 用户名: " + user.getUsername());

        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        System.out.println("密码MD5: " + encryptedPassword);
        System.out.println("数据库密码: " + user.getPassword());

        if (!user.getPassword().equals(encryptedPassword)) {
            System.out.println("登录失败 - 密码错误");
            throw new RuntimeException("密码错误");
        }

        System.out.println("密码验证成功，生成Token");

        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtil.generateToken(user.getId()));
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        System.out.println("登录成功 - Token: " + response.getToken());

        return response;
    }

    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String encryptedOldPassword = DigestUtils.md5DigestAsHex(request.getOldPassword().getBytes());
        if (!user.getPassword().equals(encryptedOldPassword)) {
            throw new RuntimeException("旧密码错误");
        }

        String encryptedNewPassword = DigestUtils.md5DigestAsHex(request.getNewPassword().getBytes());
        user.setPassword(encryptedNewPassword);
        userMapper.updateById(user);
    }

    public void updateEmail(Long userId, UpdateEmailRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new RuntimeException("密码错误");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        wrapper.ne(User::getId, userId);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("邮箱已被使用");
        }

        user.setEmail(request.getEmail());
        userMapper.updateById(user);
    }
}
