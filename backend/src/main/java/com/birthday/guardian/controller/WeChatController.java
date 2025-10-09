package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.entity.UserWechat;
import com.birthday.guardian.service.WeChatAuthService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
