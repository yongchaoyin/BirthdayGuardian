package com.birthdayguardian.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     */
    void sendVerificationCode(String to, String code);

    /**
     * 发送生日提醒邮件
     */
    void sendBirthdayReminder(String to, String roleName, String birthday, int advanceDays);
}