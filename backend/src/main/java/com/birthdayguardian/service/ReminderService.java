package com.birthdayguardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.birthdayguardian.entity.Reminder;

import java.util.List;

/**
 * 提醒服务接口
 */
public interface ReminderService extends IService<Reminder> {

    /**
     * 根据用户ID查询提醒列表
     */
    List<Reminder> listByUserId(Long userId);

    /**
     * 添加提醒
     */
    Reminder addReminder(Long userId, Long roleId, String notifyType, Integer advanceDays);

    /**
     * 删除提醒
     */
    void deleteReminder(Long id, Long userId);
}