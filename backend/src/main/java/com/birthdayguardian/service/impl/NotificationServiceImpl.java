package com.birthdayguardian.service.impl;

import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.birthdayguardian.entity.*;
import com.birthdayguardian.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 通知服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final UserService userService;
    private final RoleService roleService;
    private final ReminderService reminderService;
    private final EmailService emailService;
    private final NoticeLogService noticeLogService;

    @Override
    public void executeDailyBirthdayCheck() {
        log.info("开始执行每日生日提醒检查");
        
        try {
            // 获取所有用户
            List<User> users = userService.list(new LambdaQueryWrapper<User>()
                    .eq(User::getStatus, 1));
            
            for (User user : users) {
                try {
                    checkAndSendUserBirthdayReminders(user.getId());
                } catch (Exception e) {
                    log.error("用户{}的生日提醒检查失败：{}", user.getId(), e.getMessage());
                }
            }
            
            log.info("每日生日提醒检查完成");
        } catch (Exception e) {
            log.error("每日生日提醒检查执行失败：{}", e.getMessage());
        }
    }

    @Override
    public void checkAndSendUserBirthdayReminders(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return;
        }

        // 获取用户的所有提醒设置
        List<Reminder> reminders = reminderService.listByUserId(userId);
        
        for (Reminder reminder : reminders) {
            try {
                Role role = roleService.getById(reminder.getRoleId());
                if (role == null) {
                    continue;
                }

                // 检查是否需要发送提醒
                if (shouldSendReminder(role, reminder.getAdvanceDays())) {
                    sendBirthdayReminder(user, role, reminder);
                }
            } catch (Exception e) {
                log.error("处理提醒{}失败：{}", reminder.getId(), e.getMessage());
            }
        }
    }

    /**
     * 判断是否需要发送提醒
     */
    private boolean shouldSendReminder(Role role, int advanceDays) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate;

        if (role.getIsLunar() == 1) {
            // 农历生日处理
            targetDate = getLunarBirthdayInCurrentYear(role.getBirthday());
        } else {
            // 公历生日处理
            targetDate = role.getBirthday().withYear(today.getYear());
            
            // 如果今年的生日已过，检查明年的生日
            if (targetDate.isBefore(today)) {
                targetDate = targetDate.withYear(today.getYear() + 1);
            }
        }

        // 计算提醒日期
        LocalDate reminderDate = targetDate.minusDays(advanceDays);
        
        return today.equals(reminderDate);
    }

    /**
     * 获取农历生日在当前年份的公历日期
     */
    private LocalDate getLunarBirthdayInCurrentYear(LocalDate lunarBirthday) {
        try {
            int currentYear = LocalDate.now().getYear();
            
            // 使用Hutool的农历工具类
            ChineseDate chineseDate = new ChineseDate(currentYear, 
                    lunarBirthday.getMonthValue(), 
                    lunarBirthday.getDayOfMonth());
            
            return DateUtil.toLocalDateTime(chineseDate.getGregorianDate()).toLocalDate();
        } catch (Exception e) {
            log.warn("农历日期转换失败，使用公历日期：{}", e.getMessage());
            // 转换失败时使用公历日期
            return lunarBirthday.withYear(LocalDate.now().getYear());
        }
    }

    /**
     * 发送生日提醒
     */
    private void sendBirthdayReminder(User user, Role role, Reminder reminder) {
        try {
            String birthdayStr = role.getBirthday().format(DateTimeFormatter.ofPattern("MM月dd日"));
            if (role.getIsLunar() == 1) {
                birthdayStr += "（农历）";
            }

            // 发送邮件
            emailService.sendBirthdayReminder(
                    user.getEmail(),
                    role.getName(),
                    birthdayStr,
                    reminder.getAdvanceDays()
            );

            // 记录成功日志
            saveNoticeLog(user.getId(), role.getId(), 1, 
                    String.format("生日提醒发送成功：%s，提前%d天", role.getName(), reminder.getAdvanceDays()),
                    null);

        } catch (Exception e) {
            log.error("发送生日提醒失败，用户：{}，角色：{}，错误：{}", user.getId(), role.getId(), e.getMessage());
            
            // 记录失败日志
            saveNoticeLog(user.getId(), role.getId(), 0,
                    String.format("生日提醒发送失败：%s，提前%d天", role.getName(), reminder.getAdvanceDays()),
                    e.getMessage());
        }
    }

    /**
     * 保存通知日志
     */
    private void saveNoticeLog(Long userId, Long roleId, Integer status, String message, String errorMsg) {
        try {
            NoticeLog log = new NoticeLog();
            log.setUserId(userId);
            log.setRoleId(roleId);
            log.setSendStatus(status);
            log.setMessage(message);
            log.setErrorMsg(errorMsg);
            
            noticeLogService.save(log);
        } catch (Exception e) {
            log.error("保存通知日志失败：{}", e.getMessage());
        }
    }
}