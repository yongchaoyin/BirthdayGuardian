package com.birthdayguardian.service.impl;

import com.birthdayguardian.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.subject-prefix}")
    private String subjectPrefix;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subjectPrefix + " 邮箱验证码");
            message.setText(String.format(
                "您的验证码是：%s\n\n" +
                "验证码有效期为5分钟，请及时使用。\n" +
                "如果这不是您的操作，请忽略此邮件。\n\n" +
                "生日守护者团队",
                code
            ));
            
            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人：{}", to);
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人：{}，错误：{}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    @Override
    public void sendBirthdayReminder(String to, String roleName, String birthday, int advanceDays) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subjectPrefix + " 生日提醒");
            
            String content;
            if (advanceDays == 0) {
                content = String.format(
                    "🎉 生日提醒 🎉\n\n" +
                    "今天是 %s 的生日！\n" +
                    "生日：%s\n\n" +
                    "别忘了送上您的祝福哦～\n\n" +
                    "生日守护者",
                    roleName, birthday
                );
            } else {
                content = String.format(
                    "🎂 生日提醒 🎂\n\n" +
                    "还有 %d 天就是 %s 的生日了！\n" +
                    "生日：%s\n\n" +
                    "记得提前准备生日礼物和祝福哦～\n\n" +
                    "生日守护者",
                    advanceDays, roleName, birthday
                );
            }
            
            message.setText(content);
            mailSender.send(message);
            log.info("生日提醒邮件发送成功，收件人：{}，角色：{}", to, roleName);
        } catch (Exception e) {
            log.error("生日提醒邮件发送失败，收件人：{}，角色：{}，错误：{}", to, roleName, e.getMessage());
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }
}