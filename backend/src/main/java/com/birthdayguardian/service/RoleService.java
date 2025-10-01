package com.birthdayguardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.birthdayguardian.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> listByUserId(Long userId);

    /**
     * 添加角色
     */
    Role addRole(Long userId, String name, String birthday, Integer isLunar, String remark);

    /**
     * 更新角色
     */
    Role updateRole(Long id, String name, String birthday, Integer isLunar, String remark);

    /**
     * 删除角色
     */
    void deleteRole(Long id, Long userId);
}