package com.birthdayguardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.birthdayguardian.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(String email);

    /**
     * 发送验证码
     */
    void sendVerificationCode(String email);

    /**
     * 验证码注册
     */
    User register(String email, String code, String nickname);

    /**
     * 验证码登录
     */
    User loginByCode(String email, String code);

    /**
     * 密码登录
     */
    User loginByPassword(String email, String password);

    /**
     * 设置密码
     */
    void setPassword(Long userId, String password);
}