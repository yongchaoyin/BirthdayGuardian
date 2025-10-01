package com.birthdayguardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.birthdayguardian.entity.Reminder;
import com.birthdayguardian.mapper.ReminderMapper;
import com.birthdayguardian.service.ReminderService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提醒服务实现类
 */
@Service
public class ReminderServiceImpl extends ServiceImpl<ReminderMapper, Reminder> implements ReminderService {

    @Override
    public List<Reminder> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getUserId, userId)
                .orderByDesc(Reminder::getCreateTime));
    }

    @Override
    public Reminder addReminder(Long userId, Long roleId, String notifyType, Integer advanceDays) {
        // 检查是否已存在相同的提醒
        Reminder existReminder = this.getOne(new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getUserId, userId)
                .eq(Reminder::getRoleId, roleId)
                .eq(Reminder::getNotifyType, notifyType));
        
        if (existReminder != null) {
            throw new RuntimeException("该角色已设置相同类型的提醒");
        }
        
        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setRoleId(roleId);
        reminder.setNotifyType(notifyType);
        reminder.setAdvanceDays(advanceDays);
        
        this.save(reminder);
        return reminder;
    }

    @Override
    public void deleteReminder(Long id, Long userId) {
        Reminder reminder = this.getById(id);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        
        if (!reminder.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此提醒");
        }
        
        this.removeById(id);
    }
}