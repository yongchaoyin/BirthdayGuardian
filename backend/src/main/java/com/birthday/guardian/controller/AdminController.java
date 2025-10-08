package com.birthday.guardian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.birthday.guardian.common.Result;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取所有用户列表（分页）
     */
    @GetMapping("/users")
    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 搜索功能
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);

        // 清除密码字段
        result.getRecords().forEach(user -> user.setPassword(null));

        return Result.success(result);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public Result<?> getUserStats() {
        Long totalUsers = userMapper.selectCount(null);

        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getRole, "admin");
        Long adminCount = userMapper.selectCount(adminWrapper);

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, "user");
        Long userCount = userMapper.selectCount(userWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("adminCount", adminCount);
        stats.put("userCount", userCount);

        return Result.success(stats);
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (!"admin".equals(role) && !"user".equals(role)) {
            return Result.error("角色只能是admin或user");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setRole(role);
        userMapper.updateById(user);
        return Result.success("角色更新成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id, @RequestAttribute Long userId) {
        if (id.equals(userId)) {
            return Result.error("不能删除自己");
        }

        userMapper.deleteById(id);
        return Result.success("用户删除成功");
    }
}
