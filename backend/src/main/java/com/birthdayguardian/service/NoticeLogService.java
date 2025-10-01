package com.birthdayguardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.birthdayguardian.entity.NoticeLog;

import java.util.List;

/**
 * 通知日志服务接口
 */
public interface NoticeLogService extends IService<NoticeLog> {

    /**
     * 根据用户ID查询通知日志
     */
    List<NoticeLog> listByUserId(Long userId);
}