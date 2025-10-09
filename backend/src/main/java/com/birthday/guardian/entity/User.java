package com.birthday.guardian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    private String phone;

    private String password;

    private String role; // admin-管理员, user-普通用户

    private String membershipLevel; // FREE/VIP

    private LocalDateTime vipExpireTime; // VIP到期时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer maxRoleCount;

    @TableField(exist = false)
    private Boolean vipActive;
}
