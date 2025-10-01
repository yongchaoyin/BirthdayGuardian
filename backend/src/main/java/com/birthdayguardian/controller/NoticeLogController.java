package com.birthdayguardian.controller;

import com.birthdayguardian.common.Result;
import com.birthdayguardian.entity.NoticeLog;
import com.birthdayguardian.service.NoticeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知日志控制器
 */
@RestController
@RequestMapping("/noticeLog")
@RequiredArgsConstructor
public class NoticeLogController {

    private final NoticeLogService noticeLogService;

    /**
     * 查询通知日志列表
     */
    @GetMapping("/list")
    public Result<List<NoticeLog>> list(@RequestParam Long userId) {
        try {
            List<NoticeLog> logs = noticeLogService.listByUserId(userId);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}