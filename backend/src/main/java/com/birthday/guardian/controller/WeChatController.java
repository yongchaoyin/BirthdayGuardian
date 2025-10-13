package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.entity.UserWechat;
import com.birthday.guardian.service.WeChatAuthService;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
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

    @Autowired
    private WxMpService wxMpService;

    @Autowired(required = false)
    private WxMpMessageRouter messageRouter;

    /**
     * 微信公众号服务器配置验证
     * GET /api/wechat/portal
     */
    @GetMapping("/portal")
    public String verifyWeChatServer(
            @RequestParam(name = "signature", required = false) String signature,
            @RequestParam(name = "timestamp", required = false) String timestamp,
            @RequestParam(name = "nonce", required = false) String nonce,
            @RequestParam(name = "echostr", required = false) String echostr) {

        try {
            if (wxMpService.checkSignature(timestamp, nonce, signature)) {
                return echostr;
            }
        } catch (Exception e) {
            System.err.println("微信服务器验证失败: " + e.getMessage());
        }
        return "error";
    }

    /**
     * 接收微信公众号消息和事件
     * POST /api/wechat/portal
     */
    @PostMapping(value = "/portal", produces = "application/xml; charset=UTF-8")
    public String handleWeChatMessage(
            @RequestBody String requestBody,
            @RequestParam(name = "signature", required = false) String signature,
            @RequestParam(name = "timestamp", required = false) String timestamp,
            @RequestParam(name = "nonce", required = false) String nonce,
            @RequestParam(name = "encrypt_type", required = false) String encryptType,
            @RequestParam(name = "msg_signature", required = false) String msgSignature) {

        try {
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);

            // 如果配置了消息路由器，使用路由器处理消息
            if (messageRouter != null) {
                WxMpXmlOutMessage outMessage = messageRouter.route(inMessage);
                if (outMessage != null) {
                    return outMessage.toXml();
                }
            }

            // 默认返回空字符串表示成功接收
            return "success";
        } catch (Exception e) {
            System.err.println("处理微信消息失败: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

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
