package com.birthdayguardian.scheduler;

import com.birthdayguardian.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 生日提醒定时任务调度器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayScheduler {

    private final NotificationService notificationService;

    /**
     * 每天上午9点执行生日提醒检查
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "${app.scheduler.notify-cron:0 0 9 * * ?}")
    public void dailyBirthdayCheck() {
        log.info("定时任务开始：每日生日提醒检查");
        
        try {
            notificationService.executeDailyBirthdayCheck();
            log.info("定时任务完成：每日生日提醒检查");
        } catch (Exception e) {
            log.error("定时任务执行失败：每日生日提醒检查，错误：{}", e.getMessage(), e);
        }
    }

    /**
     * 测试用的定时任务，每分钟执行一次（仅用于开发测试）
     * 生产环境请注释掉此方法
     */
    // @Scheduled(cron = "0 * * * * ?")
    public void testBirthdayCheck() {
        log.info("测试定时任务：每分钟生日提醒检查");
        
        try {
            notificationService.executeDailyBirthdayCheck();
            log.info("测试定时任务完成：每分钟生日提醒检查");
        } catch (Exception e) {
            log.error("测试定时任务执行失败：每分钟生日提醒检查，错误：{}", e.getMessage(), e);
        }
    }
}