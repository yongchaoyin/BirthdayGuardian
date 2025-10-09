package com.birthday.guardian.config;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class WeChatMpConfig {

    @Autowired
    private WeChatMpProperties properties;

    @Bean
    public WxMpConfigStorage wxMpConfigStorage() {
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(properties.getAppId());
        config.setSecret(properties.getSecret());
        config.setToken(properties.getToken());
        config.setAesKey(properties.getAesKey());
        return config;
    }

    @Bean
    public WxMpService wxMpService(WxMpConfigStorage configStorage) {
        WxMpServiceImpl service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(configStorage);
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getSecret())) {
            System.out.println("[WeChatMpConfig] 未配置 appId/secret，将以占位模式运行。");
        }
        return service;
    }
}
