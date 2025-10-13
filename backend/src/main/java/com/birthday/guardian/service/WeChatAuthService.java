package com.birthday.guardian.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.birthday.guardian.common.MembershipLevel;
import com.birthday.guardian.config.WeChatMpProperties;
import com.birthday.guardian.dto.LoginResponse;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.entity.UserWechat;
import com.birthday.guardian.mapper.UserMapper;
import com.birthday.guardian.util.JwtUtil;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WeChatAuthService {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private UserWechatService userWechatService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatMpProperties properties;

    @Autowired
    private JwtUtil jwtUtil;

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
            // 注释掉 getUnionid() 方法调用，因为该方法在当前版本中不存在
            // wechat.setUnionid(wxUser.getUnionid());
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
        // 修复方法名，使用正确的OAuth2授权URL构建方法
        return wxMpService.getOAuth2Service().buildAuthorizationUrl(target, "snsapi_userinfo", state);
    }

    public WxJsapiSignature createJsSdkSignature(String url) {
        if (!StringUtils.hasText(url)) {
            throw new RuntimeException("签名URL不能为空");
        }
        try {
            return wxMpService.createJsapiSignature(url);
        } catch (WxErrorException e) {
            throw new RuntimeException("获取微信JS-SDK签名失败: " + e.getMessage(), e);
        }
    }

    /**
     * 微信小程序登录
     */
    @Transactional
    public LoginResponse miniProgramLogin(String code) {
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("登录code不能为空");
        }

        try {
            // 调用微信接口获取session信息
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            String openid = session.getOpenid();
            String sessionKey = session.getSessionKey();
            String unionid = session.getUnionid();

            // 查找是否已绑定用户
            UserWechat userWechat = userWechatService.getByOpenId(openid);

            User user;
            if (userWechat != null) {
                // 已绑定，直接登录
                user = userMapper.selectById(userWechat.getUserId());
                if (user == null) {
                    throw new RuntimeException("用户不存在");
                }

                // 更新session_key
                userWechat.setSessionKey(sessionKey);
                if (StringUtils.hasText(unionid)) {
                    userWechat.setUnionid(unionid);
                }
                userWechat.setUpdateTime(LocalDateTime.now());
                userWechatService.bindWechat(userWechat);
            } else {
                // 未绑定，创建新用户
                user = new User();
                user.setUsername("微信用户" + openid.substring(0, 8));
                user.setEmail(openid + "@wechat.com"); // 临时邮箱
                user.setPassword(UUID.randomUUID().toString()); // 随机密码
                user.setRole("user");
                user.setMembershipLevel(MembershipLevel.FREE.name());
                user.setInviteCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
                user.setInviteSuccessCount(0);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());

                userMapper.insert(user);

                // 创建微信绑定记录
                userWechat = new UserWechat();
                userWechat.setUserId(user.getId());
                userWechat.setOpenid(openid);
                userWechat.setSessionKey(sessionKey);
                if (StringUtils.hasText(unionid)) {
                    userWechat.setUnionid(unionid);
                }
                userWechat.setSubscribe(0);
                userWechat.setBindTime(LocalDateTime.now());
                userWechat.setUpdateTime(LocalDateTime.now());

                userWechatService.bindWechat(userWechat);
            }

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getId());

            // 构建返回数据
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            response.setRole(user.getRole());
            response.setMembershipLevel(user.getMembershipLevel());
            response.setVipExpireTime(user.getVipExpireTime());

            return response;
        } catch (WxErrorException e) {
            throw new RuntimeException("微信小程序登录失败: " + e.getMessage(), e);
        }
    }
}
