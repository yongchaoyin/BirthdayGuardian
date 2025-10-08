package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.entity.BirthdayRole;
import com.birthday.guardian.service.BirthdayRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class BirthdayRoleController {
    @Autowired
    private BirthdayRoleService birthdayRoleService;

    @PostMapping
    public Result<Void> addRole(@RequestBody BirthdayRole role, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            role.setUserId(userId);
            birthdayRoleService.addRole(role);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
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
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            birthdayRoleService.deleteRole(id, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<BirthdayRole>> getRoles(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<BirthdayRole> roles = birthdayRoleService.getRolesByUserId(userId);
            return Result.success(roles);
        } catch (Exception e) {
            return Result.error(e.getMessage());
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
