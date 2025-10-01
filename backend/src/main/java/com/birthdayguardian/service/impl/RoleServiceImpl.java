package com.birthdayguardian.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.birthdayguardian.entity.Role;
import com.birthdayguardian.mapper.RoleMapper;
import com.birthdayguardian.service.RoleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 角色服务实现类
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Role>()
                .eq(Role::getUserId, userId)
                .orderByDesc(Role::getCreateTime));
    }

    @Override
    public Role addRole(Long userId, String name, String birthday, Integer isLunar, String remark) {
        Role role = new Role();
        role.setUserId(userId);
        role.setName(name);
        role.setBirthday(LocalDate.parse(birthday));
        role.setIsLunar(isLunar);
        role.setRemark(remark);
        
        this.save(role);
        return role;
    }

    @Override
    public Role updateRole(Long id, String name, String birthday, Integer isLunar, String remark) {
        Role role = this.getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        role.setName(name);
        role.setBirthday(LocalDate.parse(birthday));
        role.setIsLunar(isLunar);
        role.setRemark(remark);
        
        this.updateById(role);
        return role;
    }

    @Override
    public void deleteRole(Long id, Long userId) {
        Role role = this.getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        if (!role.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此角色");
        }
        
        this.removeById(id);
    }
}