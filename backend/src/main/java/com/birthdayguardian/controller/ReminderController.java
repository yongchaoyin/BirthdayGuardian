package com.birthdayguardian.controller;

import com.birthdayguardian.common.Result;
import com.birthdayguardian.entity.Reminder;
import com.birthdayguardian.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 提醒控制器
 */
@RestController
@RequestMapping("/reminder")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    /**
     * 查询提醒列表
     */
    @GetMapping("/list")
    public Result<List<Reminder>> list(@RequestParam Long userId) {
        try {
            List<Reminder> reminders = reminderService.listByUserId(userId);
            return Result.success(reminders);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加提醒
     */
    @PostMapping("/add")
    public Result<Reminder> add(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long roleId = Long.valueOf(request.get("roleId").toString());
        String notifyType = (String) request.get("notifyType");
        Integer advanceDays = (Integer) request.get("advanceDays");
        
        if (notifyType == null || notifyType.trim().isEmpty()) {
            notifyType = "email"; // 默认邮件通知
        }
        if (advanceDays == null || advanceDays < 0) {
            advanceDays = 1; // 默认提前1天
        }
        
        try {
            Reminder reminder = reminderService.addReminder(userId, roleId, notifyType, advanceDays);
            return Result.success("添加成功", reminder);
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 删除提醒
     */
    @DeleteMapping("/delete/{id}")
    public Result<Object> delete(@PathVariable Long id, @RequestParam Long userId) {
        try {
            reminderService.deleteReminder(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}