package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    /**
     * 测试邮件发送
     */
    @PostMapping("/email")
    public Result<String> testEmail(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }

            String to = params.get("to");
            if (to == null || to.isEmpty()) {
                return Result.error("收件人邮箱不能为空");
            }

            String result = emailService.sendEmail(
                to,
                "生日守护者系统 - 测试邮件",
                "这是一封测试邮件，如果您收到此邮件，说明邮件配置成功！"
            );

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("发送失败: " + e.getMessage());
        }
    }
}
