package com.birthdayguardian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notice_log")
public class NoticeLog {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 发送时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime sendTime;

    /**
     * 发送状态：1-成功，0-失败
     */
    private Integer sendStatus;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 错误信息
     */
    private String errorMsg;
}