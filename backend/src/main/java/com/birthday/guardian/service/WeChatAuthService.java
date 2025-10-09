package com.birthday.guardian.service;

import com.birthday.guardian.config.WeChatMpProperties;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.entity.UserWechat;
import com.birthday.guardian.mapper.UserMapper;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class WeChatAuthService {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private UserWechatService userWechatService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatMpProperties properties;

    public UserWechat bindUser(Long userId, String code) {
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("授权code不能为空");
        }
        try {
            WxOAuth2AccessToken token = wxMpService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo wxUser = wxMpService.getOAuth2Service().getUserInfo(token, null);

            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            UserWechat wechat = userWechatService.getByUserId(userId);
            if (wechat == null) {
                wechat = new UserWechat();
                wechat.setUserId(userId);
                wechat.setBindTime(LocalDateTime.now());
            }
            wechat.setOpenid(wxUser.getOpenid());
            wechat.setUnionid(wxUser.getUnionid());
            wechat.setNickname(wxUser.getNickname());
            wechat.setAvatar(wxUser.getHeadImgUrl());
            wechat.setSessionKey(token.getAccessToken());
            wechat.setSubscribe(1);
            wechat.setUpdateTime(LocalDateTime.now());
            userWechatService.bindWechat(wechat);
            return wechat;
        } catch (WxErrorException e) {
            throw new RuntimeException("微信授权失败: " + e.getMessage(), e);
        }
    }

    public String buildOauthUrl(String redirectUrl, String state) {
        String target = StringUtils.hasText(redirectUrl) ? redirectUrl : properties.getReminderUrl();
        return wxMpService.oauth2buildAuthorizationUrl(target, "snsapi_userinfo", state);
    }
}
