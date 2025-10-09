package com.birthday.guardian.service;

import com.birthday.guardian.config.WeChatMpProperties;
import com.birthday.guardian.entity.BirthdayRole;
import com.birthday.guardian.entity.NotificationLog;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.entity.UserWechat;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class WeChatTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WeChatMpProperties properties;

    @Autowired
    private UserWechatService userWechatService;

    @Autowired
    private NotificationLogService notificationLogService;

    public boolean sendBirthdayReminder(User user, BirthdayRole role, LocalDate upcomingBirthday, long daysUntilBirthday) {
        UserWechat userWechat = userWechatService.getByUserId(user.getId());
        if (userWechat == null || !StringUtils.hasText(userWechat.getOpenid())) {
            return false;
        }
        if (!StringUtils.hasText(properties.getBirthdayTemplateId())) {
            return false;
        }

        String remark = role.getRemark();
        if (!StringUtils.hasText(remark)) {
            remark = "生日守护者祝福您生日快乐！";
        }

        WxMpTemplateMessage message = WxMpTemplateMessage.builder()
                .toUser(userWechat.getOpenid())
                .templateId(properties.getBirthdayTemplateId())
                .url(StringUtils.hasText(properties.getReminderUrl()) ? properties.getReminderUrl() : null)
                .build();

        message.addData(new WxMpTemplateData("first", String.format("亲爱的%s，%s将在%d天后迎来生日。", user.getUsername(), role.getRoleName(), daysUntilBirthday)));
        message.addData(new WxMpTemplateData("keyword1", role.getRoleName()));
        message.addData(new WxMpTemplateData("keyword2", DATE_FORMATTER.format(upcomingBirthday)));
        message.addData(new WxMpTemplateData("remark", remark));

        boolean success = false;
        try {
            wxMpService.getTemplateMsgService().sendTemplateMsg(message);
            success = true;
        } catch (WxErrorException e) {
            System.err.println("发送微信模板消息失败: " + e.getMessage());
        }

        NotificationLog log = new NotificationLog();
        log.setUserId(user.getId());
        log.setRoleId(role.getId());
        log.setChannel("WECHAT");
        log.setStatus(success ? "SUCCESS" : "FAILURE");
        log.setTitle("生日守护者微信提醒");
        log.setContentPreview(remark.length() > 480 ? remark.substring(0, 480) : remark);
        log.setEventDate(upcomingBirthday);
        notificationLogService.recordNotification(log);
        return success;
    }
}
