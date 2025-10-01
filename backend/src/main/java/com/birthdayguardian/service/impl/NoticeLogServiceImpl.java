package com.birthdayguardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.birthdayguardian.entity.NoticeLog;
import com.birthdayguardian.mapper.NoticeLogMapper;
import com.birthdayguardian.service.NoticeLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知日志服务实现类
 */
@Service
public class NoticeLogServiceImpl extends ServiceImpl<NoticeLogMapper, NoticeLog> implements NoticeLogService {

    @Override
    public List<NoticeLog> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<NoticeLog>()
                .eq(NoticeLog::getUserId, userId)
                .orderByDesc(NoticeLog::getSendTime));
    }
}