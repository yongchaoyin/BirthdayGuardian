package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.entity.UserWechat;
import com.birthday.guardian.service.WeChatAuthService;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wechat")
public class WeChatController {

    @Autowired
    private WeChatAuthService weChatAuthService;

    @GetMapping("/oauth-url")
    public Result<?> getOauthUrl(@RequestParam(required = false) String redirect,
                                 @RequestParam(required = false) String state) {
        String url = weChatAuthService.buildOauthUrl(redirect, state);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success(data);
    }

    @PostMapping("/bind")
    public Result<?> bindWechat(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String code = body.get("code");
            UserWechat wechat = weChatAuthService.bindUser(userId, code);
            return Result.success(wechat);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/js-sdk-config")
    public Result<?> getJsSdkConfig(@RequestParam String url) {
        if (!StringUtils.hasText(url)) {
            return Result.error("url参数不能为空");
        }
        try {
            WxJsapiSignature signature = weChatAuthService.createJsSdkSignature(url);
            Map<String, Object> data = new HashMap<>();
            data.put("appId", signature.getAppId());
            data.put("timestamp", signature.getTimestamp());
            data.put("nonceStr", signature.getNonceStr());
            data.put("signature", signature.getSignature());
            data.put("url", signature.getUrl());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
