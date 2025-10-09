package com.birthday.guardian.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.mp")
public class WeChatMpProperties {
    private String appId;
    private String secret;
    private String token;
    private String aesKey;
    private String birthdayTemplateId;
    private String reminderUrl;
}
