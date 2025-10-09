package com.birthday.guardian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_wechat")
public class UserWechat {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String openid;

    private String unionid;

    private String sessionKey;

    private String nickname;

    private String avatar;

    private Integer subscribe;

    private LocalDateTime bindTime;

    private LocalDateTime updateTime;
}
