package com.birthday.guardian.service;

import com.birthday.guardian.entity.NotificationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SmsService {

    @Autowired
    private NotificationLogService notificationLogService;

    public boolean sendSms(String phone, String content, Long userId, Long roleId, LocalDate eventDate) {
        boolean success = false;
        if (phone != null && !phone.trim().isEmpty()) {
            // TODO: integrate with real SMS provider. For now, log to console.
            System.out.println("[SMS] Sending to " + phone + ": " + content);
            success = true;
        } else {
            System.out.println("[SMS] Missing phone number, skip sending.");
        }

        NotificationLog log = new NotificationLog();
        log.setUserId(userId);
        log.setRoleId(roleId);
        log.setChannel("SMS");
        log.setStatus(success ? "SUCCESS" : "FAILURE");
        log.setTitle("生日守护者短信提醒");
        log.setContentPreview(content.length() > 480 ? content.substring(0, 480) : content);
        log.setEventDate(eventDate != null ? eventDate : LocalDate.now());
        notificationLogService.recordNotification(log);

        return success;
    }
}
