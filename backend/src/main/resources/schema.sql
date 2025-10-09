-- 创建数据库
CREATE DATABASE IF NOT EXISTS birthday_guardian CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE birthday_guardian;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：admin-管理员，user-普通用户',
  `membership_level` VARCHAR(20) NOT NULL DEFAULT 'FREE' COMMENT '会员等级：FREE/VIP',
  `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP到期时间',
  `invite_code` VARCHAR(20) NOT NULL COMMENT '推广码',
  `invited_by` BIGINT DEFAULT NULL COMMENT '邀请人用户ID',
  `invite_success_count` INT NOT NULL DEFAULT 0 COMMENT '成功邀请次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_invited_by` (`invited_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 生日角色表
CREATE TABLE IF NOT EXISTS `birthday_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_type` VARCHAR(50) NOT NULL COMMENT '角色类型',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `birth_date` DATE NOT NULL COMMENT '出生日期',
  `calendar_type` TINYINT NOT NULL DEFAULT 1 COMMENT '日历类型：1-阳历，2-阴历',
  `lunar_birth_date` VARCHAR(20) COMMENT '阴历生日',
  `remind_days` INT NOT NULL DEFAULT 3 COMMENT '提前提醒天数',
  `role_phone` VARCHAR(20) DEFAULT NULL COMMENT '角色联系电话',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生日角色表';

-- 公告表
CREATE TABLE IF NOT EXISTS `announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 通知发送日志表
CREATE TABLE IF NOT EXISTS `notification_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '生日角色ID',
  `channel` VARCHAR(20) NOT NULL COMMENT '通知渠道：EMAIL/SMS/WECHAT',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '发送状态',
  `title` VARCHAR(200) COMMENT '通知标题',
  `content_preview` VARCHAR(500) COMMENT '通知内容预览',
  `event_date` DATE COMMENT '关联事件日期，比如生日',
  `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_channel` (`channel`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_event_date` (`event_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知发送日志表';

-- 微信绑定表
CREATE TABLE IF NOT EXISTS `user_wechat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
  `session_key` VARCHAR(128) DEFAULT NULL COMMENT '会话密钥/凭证',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '微信昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `subscribe` TINYINT DEFAULT 0 COMMENT '是否关注公众号：0-否 1-是',
  `bind_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与微信绑定表';

-- 插入默认管理员账号 (密码: admin@123)
INSERT INTO `user` (`username`, `email`, `password`, `role`)
VALUES ('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin')
ON DUPLICATE KEY UPDATE `role` = 'admin';
