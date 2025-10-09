package com.birthday.guardian.service;

import com.birthday.guardian.entity.NotificationLog;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Slf4j
@Service
public class SmsService {

    @Autowired
    private NotificationLogService notificationLogService;

    public boolean sendSms(String phone, String content, Long userId, Long roleId, LocalDate eventDate) {
        boolean success = false;
        if (StringUtils.hasText(phone)) {
            try {
                SmsBlend smsBlend = SmsFactory.getSmsBlend();
                if (smsBlend != null) {
                    SmsResponse response = smsBlend.sendMessage(phone.trim(), content);
                    if (response == null) {
                        log.warn("短信发送失败，手机号：{}，未收到任何响应", phone);
                    } else {
                        success = response.isSuccess();
                        if (!success) {
                            log.warn("短信发送失败，手机号：{}，响应：{}", phone, response.getData());
                        }
                    }
                } else {
                    log.warn("未检测到可用的 SmsBlend 配置，暂以控制台日志方式模拟发送");
                    log.info("[SMS] {} => {}", phone, content);
                    success = true;
                }
            } catch (Exception ex) {
                log.error("短信发送异常，手机号：{}", phone, ex);
            }
        } else {
            log.info("[SMS] 缺少手机号，跳过发送");
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
