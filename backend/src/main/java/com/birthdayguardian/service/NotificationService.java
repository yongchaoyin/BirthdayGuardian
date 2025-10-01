package com.birthdayguardian.service;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 执行每日生日提醒检查
     */
    void executeDailyBirthdayCheck();

    /**
     * 检查并发送单个用户的生日提醒
     */
    void checkAndSendUserBirthdayReminders(Long userId);
}