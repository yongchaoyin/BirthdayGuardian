package com.birthday.guardian.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class WeChatMiniAppConfig {

    @Autowired
    private WeChatMiniAppProperties properties;

    @Bean
    public WxMaConfig wxMaConfig() {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(properties.getAppId());
        config.setSecret(properties.getSecret());
        return config;
    }

    @Bean
    public WxMaService wxMaService(WxMaConfig config) {
        WxMaServiceImpl service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getSecret())) {
            System.out.println("[WeChatMiniAppConfig] 未配置小程序 appId/secret，将以占位模式运行。");
        }
        return service;
    }
}
