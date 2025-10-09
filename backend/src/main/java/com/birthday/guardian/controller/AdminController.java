package com.birthday.guardian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.birthday.guardian.common.MembershipLevel;
import com.birthday.guardian.common.Result;
import com.birthday.guardian.dto.AdminDashboardStats;
import com.birthday.guardian.dto.BroadcastEmailRequest;
import com.birthday.guardian.dto.BroadcastSmsRequest;
import com.birthday.guardian.dto.NotificationTrendResponse;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.mapper.UserMapper;
import com.birthday.guardian.service.AdminDashboardService;
import com.birthday.guardian.service.EmailService;
import com.birthday.guardian.service.NotificationLogService;
import com.birthday.guardian.service.SmsService;
import com.birthday.guardian.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;
import com.birthday.guardian.entity.NotificationLog;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private NotificationLogService notificationLogService;

    /**
     * 获取所有用户列表（分页）
     */
    @GetMapping("/users")
    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestAttribute Long userId) {
        try {
            if (!isAdmin(userId)) {
                return Result.error(403, "仅管理员可以执行此操作");
            }

            Page<User> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w.like(User::getUsername, keyword)
                        .or()
                        .like(User::getEmail, keyword));
            }

            wrapper.orderByDesc(User::getCreateTime);
            Page<User> result = userMapper.selectPage(page, wrapper);

            result.getRecords().forEach(user -> {
                user.setPassword(null);
                MembershipLevel level = MembershipLevel.fromCode(user.getMembershipLevel());
                user.setVipActive(level.isVip());
                user.setMaxRoleCount(level.getMaxRoleCount());
            });

            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("查询用户列表失败");
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public Result<?> getUserStats(@RequestAttribute Long userId) {
        try {
            if (!isAdmin(userId)) {
                return Result.error(403, "仅管理员可以执行此操作");
            }
            AdminDashboardStats stats = adminDashboardService.collectStats();
            return Result.success(stats);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("获取统计信息失败");
        }
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body, @RequestAttribute Long userId) {
        try {
            if (!isAdmin(userId)) {
                return Result.error(403, "仅管理员可以执行此操作");
            }
            String role = body.get("role");
            if (!"admin".equals(role) && !"user".equals(role)) {
                return Result.error(400, "角色只能是admin或user");
            }

            User user = userMapper.selectById(id);
            if (user == null) {
                return Result.error(400, "用户不存在");
            }

            user.setRole(role);
            userMapper.updateById(user);
            return Result.success("角色更新成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("更新角色失败");
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            if (!isAdmin(userId)) {
                return Result.error(403, "仅管理员可以执行此操作");
            }
            if (id.equals(userId)) {
                return Result.error(400, "不能删除自己");
            }

            userMapper.deleteById(id);
            return Result.success("用户删除成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("删除用户失败");
        }
    }

    /**
     * 更新用户会员等级
     */
    @PutMapping("/users/{id}/membership")
    public Result<?> updateUserMembership(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          @RequestAttribute Long userId) {
        try {
            if (!isAdmin(userId)) {
                return Result.error(403, "仅管理员可以执行此操作");
            }
            String levelCode = body.get("level");
            if (levelCode == null) {
                return Result.error(400, "会员等级不能为空");
            }
            if (!"VIP".equalsIgnoreCase(levelCode) && !"FREE".equalsIgnoreCase(levelCode)) {
                return Result.error(400, "会员等级只能是FREE或VIP");
            }
            MembershipLevel level = MembershipLevel.fromCode(levelCode);
            userService.updateMembership(id, level);
            User user = userService.getUserById(id);
            if (user != null) {
                user.setPassword(null);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("message", "会员状态更新成功");
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("更新会员等级失败");
        }
    }

    /**
     * 管理员群发邮件
     */
    @PostMapping("/notifications/email")
    public Result<?> broadcastEmail(@Validated @RequestBody BroadcastEmailRequest request,
                                    @RequestAttribute Long userId) {
        try {
            if (!isAdmin(userId)) {
                return Result.error(403, "仅管理员可以执行此操作");
            }
            int total = 0;
            int success = 0;
            for (User user : userMapper.selectList(null)) {
                if (!StringUtils.hasText(user.getEmail())) {
                    continue;
                }
                total++;
                boolean result = emailService.sendEmail(user.getEmail(), request.getSubject(), request.getContent());
                if (result) {
                    success++;
                }
                NotificationLog log = new NotificationLog();
                log.setUserId(user.getId());
                log.setChannel("EMAIL");
                log.setStatus(result ? "SUCCESS" : "FAILURE");
                log.setTitle(request.getSubject());
                String preview = request.getContent();
                log.setContentPreview(preview.length() > 480 ? preview.substring(0, 480) : preview);
                log.setEventDate(java.time.LocalDate.now());
                notificationLogService.recordNotification(log);
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("targetCount", total);
            payload.put("successCount", success);
            return Result.success(payload);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("群发邮件失败");
        }
    }

    /**
     * 管理员群发短信
     */
    @PostMapping("/notifications/sms")
    public Result<?> broadcastSms(@Validated @RequestBody BroadcastSmsRequest request,
                                  @RequestAttribute Long userId) {
        try {
            if (!isAdmin(userId)) {
                return Result.error(403, "仅管理员可以执行此操作");
            }
            int total = 0;
            int success = 0;
            for (User user : userMapper.selectList(null)) {
                if (!StringUtils.hasText(user.getPhone())) {
                    continue;
                }
                total++;
                boolean result = smsService.sendSms(user.getPhone(), request.getContent(), user.getId(), null, java.time.LocalDate.now());
                if (result) {
                    success++;
                }
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("targetCount", total);
            payload.put("successCount", success);
            return Result.success(payload);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("群发短信失败");
        }
    }

    /**
     * 通知趋势统计
     */
    @GetMapping("/notifications/stats")
    public Result<?> getNotificationStats(@RequestParam(defaultValue = "14") int days,
                                          @RequestAttribute Long userId) {
        try {
            if (!isAdmin(userId)) {
                return Result.error(403, "仅管理员可以执行此操作");
            }
            NotificationTrendResponse response = new NotificationTrendResponse();
            response.setPoints(notificationLogService.getNotificationTrend(days));
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("获取通知统计失败");
        }
    }

    private boolean isAdmin(Long userId) {
        User current = userMapper.selectById(userId);
        return current != null && "admin".equals(current.getRole());
    }
}
