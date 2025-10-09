package com.birthday.guardian.service;

import com.birthday.guardian.common.MembershipLevel;
import com.birthday.guardian.entity.BirthdayRole;
import com.birthday.guardian.entity.NotificationLog;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.util.BirthdayDateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService implements ApplicationRunner {

    private Session session = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private String port;

    @Autowired
    private BirthdayRoleService birthdayRoleService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationLogService notificationLogService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private WeChatTemplateService weChatTemplateService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 邮件服务器配置
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");

        // 如果端口是465，使用SSL；如果是587，使用STARTTLS
        if ("465".equals(port)) {
            properties.put("mail.smtp.ssl.enable", "true");
        } else {
            properties.put("mail.smtp.starttls.enable", "true");
        }

        // 创建会话
        session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        System.out.println("邮件服务配置完成: " + host + ":" + port);
    }

    public boolean sendEmail(String to, String subject, String body) {
        try {
            // 创建邮件消息
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username)); // 设置发件人
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 设置收件人
            message.setSubject(subject); // 设置主题
            message.setText(body); // 设置正文

            // 发送邮件
            Transport.send(message);
            System.out.println("邮件发送成功！收件人: " + to);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("邮件发送失败！");
            return false;
        }
    }

    @Scheduled(cron = "${schedule.check-time}")
    public void checkBirthdaysAndSendReminders() {
        if (session == null) {
            System.out.println("邮件服务未初始化，跳过生日检查");
            return;
        }

        List<BirthdayRole> allRoles = birthdayRoleService.getAllRoles();
        LocalDate today = LocalDate.now();

        System.out.println("开始检查生日提醒，当前日期: " + today);

        for (BirthdayRole role : allRoles) {
            try {
                LocalDate upcomingBirthday = BirthdayDateUtil.resolveUpcomingBirthday(role, today);
                long daysUntilBirthday = java.time.temporal.ChronoUnit.DAYS.between(today, upcomingBirthday);

                if (daysUntilBirthday == role.getRemindDays()) {
                    User user = userService.getUserById(role.getUserId());
                    if (user != null) {
                        String subject = "🎂 温馨提醒：" + role.getRoleName() + "的生日小期待";
                        String content = buildWarmEmailBody(user, role, upcomingBirthday, daysUntilBirthday);

                        boolean success = sendEmail(user.getEmail(), subject, content);

                        NotificationLog log = new NotificationLog();
                        log.setUserId(user.getId());
                        log.setRoleId(role.getId());
                        log.setChannel("EMAIL");
                        log.setStatus(success ? "SUCCESS" : "FAILURE");
                        log.setTitle(subject);
                        log.setContentPreview(content.length() > 480 ? content.substring(0, 480) : content);
                        log.setEventDate(upcomingBirthday);
                        notificationLogService.recordNotification(log);

                        weChatTemplateService.sendBirthdayReminder(user, role, upcomingBirthday, daysUntilBirthday);
                    }
                }

                if (daysUntilBirthday == 0 && role.getRolePhone() != null && !role.getRolePhone().trim().isEmpty()) {
                    User user = userService.getUserById(role.getUserId());
                    if (user != null) {
                        MembershipLevel level = MembershipLevel.fromCode(user.getMembershipLevel());
                        if (level.isVip()) {
                            String smsContent = buildSmsContent(user, role, upcomingBirthday);
                            smsService.sendSms(role.getRolePhone(), smsContent, user.getId(), role.getId(), upcomingBirthday);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String buildWarmEmailBody(User user, BirthdayRole role, LocalDate upcomingBirthday, long daysUntilBirthday) {
        StringBuilder builder = new StringBuilder();
        builder.append("亲爱的 ").append(user.getUsername()).append("，\n\n");
        builder.append("感谢你把这些珍贵的关系托付给我们。\n");
        builder.append(role.getRoleName()).append("（").append(role.getRoleType()).append("）将在 ")
                .append(daysUntilBirthday)
                .append(" 天后，也就是 ")
                .append(DATE_FORMATTER.format(upcomingBirthday))
                .append(" 收到来自你的温暖祝福。\n\n");
        builder.append("贴心提示：");
        if (role.getRemark() != null && !role.getRemark().isEmpty()) {
            builder.append(role.getRemark());
        } else {
            builder.append("也许可以准备一张手写卡片、一个电话，或是一份小惊喜，让这份牵挂更有温度。");
        }
        builder.append("\n\n");
        builder.append("出生日期：").append(role.getBirthDate()).append("\n");
        builder.append("提醒设置：提前 ").append(role.getRemindDays()).append(" 天提醒\n\n");
        builder.append("无论距离远近，心意都不会迟到。生日守护者会一直陪你守护每个重要的感动时刻。\n\n");
        builder.append("— 生日守护者 · 温暖每一个特别的日子");
        return builder.toString();
    }

    private String buildSmsContent(User user, BirthdayRole role, LocalDate upcomingBirthday) {
        String remark = role.getRemark();
        if (remark == null || remark.trim().isEmpty()) {
            remark = "来自" + user.getUsername() + "的特别祝福，祝你生日快乐！";
        }
        return String.format("%s好，今天是您的生日 (%s)。%s", role.getRoleName(), DATE_FORMATTER.format(upcomingBirthday), remark);
    }
}
