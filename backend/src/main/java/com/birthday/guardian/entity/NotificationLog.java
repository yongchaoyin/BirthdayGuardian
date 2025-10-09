package com.birthday.guardian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("notification_log")
public class NotificationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long roleId;

    private String channel; // EMAIL/SMS

    private String status; // SUCCESS/FAILURE

    private String title;

    private String contentPreview;

    private LocalDate eventDate; // 对应的生日日期（阳历）

    private LocalDateTime sendTime;
}
