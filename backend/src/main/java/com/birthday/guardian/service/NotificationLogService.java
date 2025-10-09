package com.birthday.guardian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.birthday.guardian.dto.NotificationLogSummary;
import com.birthday.guardian.dto.NotificationTrendPoint;
import com.birthday.guardian.entity.NotificationLog;
import com.birthday.guardian.mapper.NotificationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationLogService {

    @Autowired
    private NotificationLogMapper notificationLogMapper;

    public void recordNotification(NotificationLog log) {
        log.setSendTime(LocalDateTime.now());
        notificationLogMapper.insert(log);
    }

    public long countByDateAndChannel(LocalDate date, String channel) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<NotificationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLog::getChannel, channel)
               .eq(NotificationLog::getStatus, "SUCCESS")
               .ge(NotificationLog::getSendTime, start)
               .lt(NotificationLog::getSendTime, end);
        return notificationLogMapper.selectCount(wrapper);
    }

    public List<NotificationTrendPoint> getNotificationTrend(int days) {
        if (days <= 0) {
            days = 7;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<NotificationLogSummary> summaries = notificationLogMapper.selectSummary(start, end);

        Map<LocalDate, NotificationTrendPoint> trendMap = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            NotificationTrendPoint point = new NotificationTrendPoint();
            point.setDate(date);
            point.setEmailCount(0L);
            point.setSmsCount(0L);
            point.setWechatCount(0L);
            trendMap.put(date, point);
        }

        for (NotificationLogSummary summary : summaries) {
            NotificationTrendPoint point = trendMap.get(summary.getSummaryDate());
            if (point == null) {
                continue;
            }
            long total = summary.getTotal() == null ? 0L : summary.getTotal();
            if ("EMAIL".equalsIgnoreCase(summary.getChannel())) {
                point.setEmailCount(total);
            } else if ("SMS".equalsIgnoreCase(summary.getChannel())) {
                point.setSmsCount(total);
            } else if ("WECHAT".equalsIgnoreCase(summary.getChannel())) {
                point.setWechatCount(total);
            }
        }

        return new ArrayList<>(trendMap.values());
    }
}
