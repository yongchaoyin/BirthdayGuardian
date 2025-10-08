package com.birthday.guardian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.birthday.guardian.entity.BirthdayRole;
import com.birthday.guardian.mapper.BirthdayRoleMapper;
import com.birthday.guardian.util.LunarUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BirthdayRoleService {
    @Autowired
    private BirthdayRoleMapper birthdayRoleMapper;

    public void addRole(BirthdayRole role) {
        LambdaQueryWrapper<BirthdayRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BirthdayRole::getUserId, role.getUserId());
        long count = birthdayRoleMapper.selectCount(wrapper);

        if (count >= 20) {
            throw new RuntimeException("每个用户最多只能添加20个角色");
        }

        if (role.getCalendarType() == 1) {
            // 阳历转阴历
            LunarUtil.LunarDate lunar = LunarUtil.solarToLunar(role.getBirthDate());
            role.setLunarBirthDate(lunar.toString());
        } else {
            // 如果输入的是阴历，直接存储
            role.setLunarBirthDate(String.format("%d-%02d-%02d",
                role.getBirthDate().getYear(),
                role.getBirthDate().getMonthValue(),
                role.getBirthDate().getDayOfMonth()));
        }

        birthdayRoleMapper.insert(role);
    }

    public void updateRole(BirthdayRole role) {
        BirthdayRole existing = birthdayRoleMapper.selectById(role.getId());
        if (existing == null) {
            throw new RuntimeException("角色不存在");
        }

        if (!existing.getUserId().equals(role.getUserId())) {
            throw new RuntimeException("无权修改此角色");
        }

        if (role.getCalendarType() == 1) {
            // 阳历转阴历
            LunarUtil.LunarDate lunar = LunarUtil.solarToLunar(role.getBirthDate());
            role.setLunarBirthDate(lunar.toString());
        } else {
            // 如果输入的是阴历，直接存储
            role.setLunarBirthDate(String.format("%d-%02d-%02d",
                role.getBirthDate().getYear(),
                role.getBirthDate().getMonthValue(),
                role.getBirthDate().getDayOfMonth()));
        }

        birthdayRoleMapper.updateById(role);
    }

    public void deleteRole(Long roleId, Long userId) {
        BirthdayRole role = birthdayRoleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        if (!role.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此角色");
        }

        birthdayRoleMapper.deleteById(roleId);
    }

    public List<BirthdayRole> getRolesByUserId(Long userId) {
        LambdaQueryWrapper<BirthdayRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BirthdayRole::getUserId, userId);
        wrapper.orderByDesc(BirthdayRole::getCreateTime);
        return birthdayRoleMapper.selectList(wrapper);
    }

    public BirthdayRole getRoleById(Long roleId) {
        return birthdayRoleMapper.selectById(roleId);
    }

    public List<BirthdayRole> getAllRoles() {
        return birthdayRoleMapper.selectList(null);
    }
}
