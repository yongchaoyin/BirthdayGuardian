package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.entity.BirthdayRole;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.service.BirthdayRoleService;
import com.birthday.guardian.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class BirthdayRoleController {
    @Autowired
    private BirthdayRoleService birthdayRoleService;

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<Void> addRole(@RequestBody BirthdayRole role, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            role.setUserId(userId);
            birthdayRoleService.addRole(role);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("添加角色失败");
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody BirthdayRole role, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            role.setId(id);
            role.setUserId(userId);
            birthdayRoleService.updateRole(role);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("更新角色失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            birthdayRoleService.deleteRole(id, userId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("删除角色失败");
        }
    }

    @GetMapping
    public Result<Map<String, Object>> getRoles(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<BirthdayRole> roles = birthdayRoleService.getRolesByUserId(userId);
            User user = userService.getUserById(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("roles", roles);
            if (user != null) {
                data.put("membershipLevel", user.getMembershipLevel());
                data.put("vipActive", user.getVipActive());
                data.put("maxRoleCount", user.getMaxRoleCount());
                data.put("inviteCode", user.getInviteCode());
                data.put("inviteSuccessCount", user.getInviteSuccessCount());
                data.put("inviteBonus", user.getInviteBonus());
                data.put("invitedBy", user.getInvitedBy());
            } else {
                data.put("membershipLevel", "FREE");
                data.put("vipActive", false);
                data.put("maxRoleCount", 3);
                data.put("inviteCode", null);
                data.put("inviteSuccessCount", 0);
                data.put("inviteBonus", 0);
                data.put("invitedBy", null);
            }
            data.put("currentCount", roles.size());
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("获取角色列表失败");
        }
    }

    @GetMapping("/{id}")
    public Result<BirthdayRole> getRole(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            BirthdayRole role = birthdayRoleService.getRoleById(id);
            if (role == null || !role.getUserId().equals(userId)) {
                return Result.error("角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
