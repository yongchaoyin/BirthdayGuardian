package com.birthday.guardian.service;

import com.birthday.guardian.entity.BirthdayRole;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.util.LunarUtil;
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
import java.util.List;
import java.util.Properties;

@Service
public class EmailService implements ApplicationRunner {

    private Session session = null;

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

    public String sendEmail(String to, String subject, String body) {
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
            return "邮件发送成功！";
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("邮件发送失败！");
            return "邮件发送失败！";
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
                LocalDate birthdayThisYear;

                if (role.getCalendarType() == 1) {
                    // 阳历生日
                    birthdayThisYear = LocalDate.of(
                        today.getYear(),
                        role.getBirthDate().getMonth(),
                        role.getBirthDate().getDayOfMonth()
                    );
                } else {
                    // 阴历生日，需要转换为阳历
                    String[] parts = role.getLunarBirthDate().split("-");
                    int lunarMonth = Integer.parseInt(parts[1]);
                    int lunarDay = Integer.parseInt(parts[2]);

                    birthdayThisYear = LunarUtil.lunarToSolar(today.getYear(), lunarMonth, lunarDay);
                }

                long daysUntilBirthday = java.time.temporal.ChronoUnit.DAYS.between(today, birthdayThisYear);

                if (daysUntilBirthday == role.getRemindDays()) {
                    User user = userService.getUserById(role.getUserId());
                    if (user != null) {
                        String subject = "生日提醒 - " + role.getRoleName();
                        String content = String.format(
                            "亲爱的 %s，\n\n" +
                            "这是一个友好的提醒：\n" +
                            "%s（%s）的生日将在 %d 天后（%s）到来。\n\n" +
                            "出生日期：%s\n" +
                            "备注：%s\n\n" +
                            "别忘了准备礼物哦！\n\n" +
                            "生日守护者系统",
                            user.getUsername(),
                            role.getRoleName(),
                            role.getRoleType(),
                            role.getRemindDays(),
                            birthdayThisYear,
                            role.getBirthDate(),
                            role.getRemark() != null ? role.getRemark() : "无"
                        );

                        sendEmail(user.getEmail(), subject, content);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
