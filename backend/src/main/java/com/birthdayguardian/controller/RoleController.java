package com.birthdayguardian.controller;

import com.birthdayguardian.common.Result;
import com.birthdayguardian.entity.Role;
import com.birthdayguardian.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 查询角色列表
     */
    @GetMapping("/list")
    public Result<List<Role>> list(@RequestParam Long userId) {
        try {
            List<Role> roles = roleService.listByUserId(userId);
            return Result.success(roles);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加角色
     */
    @PostMapping("/add")
    public Result<Role> add(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String name = (String) request.get("name");
        String birthday = (String) request.get("birthday");
        Integer isLunar = (Integer) request.get("isLunar");
        String remark = (String) request.get("remark");
        
        if (name == null || name.trim().isEmpty()) {
            return Result.badRequest("姓名不能为空");
        }
        if (birthday == null || birthday.trim().isEmpty()) {
            return Result.badRequest("生日不能为空");
        }
        
        try {
            Role role = roleService.addRole(userId, name, birthday, isLunar, remark);
            return Result.success("添加成功", role);
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/update/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String birthday = (String) request.get("birthday");
        Integer isLunar = (Integer) request.get("isLunar");
        String remark = (String) request.get("remark");
        
        if (name == null || name.trim().isEmpty()) {
            return Result.badRequest("姓名不能为空");
        }
        if (birthday == null || birthday.trim().isEmpty()) {
            return Result.badRequest("生日不能为空");
        }
        
        try {
            Role role = roleService.updateRole(id, name, birthday, isLunar, remark);
            return Result.success("更新成功", role);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/delete/{id}")
    public Result<Object> delete(@PathVariable Long id, @RequestParam Long userId) {
        try {
            roleService.deleteRole(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}