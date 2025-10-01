-- 生日守护者数据库初始化脚本
-- Birthday Guardian Database Initialization Script

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表 (Users Table)
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 生日角色表 (Birthday Roles Table)
-- ----------------------------
DROP TABLE IF EXISTS `birthday_role`;
CREATE TABLE `birthday_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '角色姓名',
  `birthday` date NOT NULL COMMENT '生日（公历）',
  `lunar_birthday` date DEFAULT NULL COMMENT '农历生日（转换为公历）',
  `is_lunar` tinyint NOT NULL DEFAULT '0' COMMENT '是否农历：0-公历，1-农历',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_birthday` (`birthday`),
  KEY `idx_lunar_birthday` (`lunar_birthday`),
  KEY `idx_is_lunar` (`is_lunar`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_birthday_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生日角色表';

-- ----------------------------
-- 提醒设置表 (Reminder Settings Table)
-- ----------------------------
DROP TABLE IF EXISTS `reminder`;
CREATE TABLE `reminder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `notify_type` varchar(20) NOT NULL DEFAULT 'EMAIL' COMMENT '通知类型：EMAIL-邮件',
  `advance_days` int NOT NULL DEFAULT '0' COMMENT '提前天数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_notify_type` (`notify_type`),
  KEY `idx_advance_days` (`advance_days`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_reminder_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reminder_role` FOREIGN KEY (`role_id`) REFERENCES `birthday_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒设置表';

-- ----------------------------
-- 通知日志表 (Notice Log Table)
-- ----------------------------
DROP TABLE IF EXISTS `notice_log`;
CREATE TABLE `notice_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `notify_type` varchar(20) NOT NULL COMMENT '通知类型：EMAIL-邮件',
  `content` text NOT NULL COMMENT '通知内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '发送状态：0-失败，1-成功',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_notify_type` (`notify_type`),
  KEY `idx_status` (`status`),
  KEY `idx_send_time` (`send_time`),
  CONSTRAINT `fk_notice_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_notice_log_role` FOREIGN KEY (`role_id`) REFERENCES `birthday_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知日志表';

-- ----------------------------
-- 插入示例数据 (Sample Data)
-- ----------------------------

-- 插入测试用户
INSERT INTO `user` (`email`, `nickname`, `password`, `status`) VALUES
('admin@example.com', '管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyiE.rV8QLjhJdJIWTKdJzKIZAm', 1),
('user@example.com', '普通用户', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyiE.rV8QLjhJdJIWTKdJzKIZAm', 1);
-- 注意：上面的密码是 "123456" 的BCrypt加密结果

-- 插入示例生日角色
INSERT INTO `birthday_role` (`user_id`, `name`, `birthday`, `is_lunar`, `remark`) VALUES
(1, '张三', '1990-05-15', 0, '好朋友'),
(1, '李四', '1985-08-20', 0, '同事'),
(1, '王五', '1992-03-10', 1, '表弟（农历生日）'),
(2, '赵六', '1988-12-25', 0, '朋友');

-- 插入示例提醒设置
INSERT INTO `reminder` (`user_id`, `role_id`, `notify_type`, `advance_days`) VALUES
(1, 1, 'EMAIL', 1),
(1, 1, 'EMAIL', 7),
(1, 2, 'EMAIL', 3),
(1, 3, 'EMAIL', 0),
(2, 4, 'EMAIL', 1);

-- 插入示例通知日志
INSERT INTO `notice_log` (`user_id`, `role_id`, `notify_type`, `content`, `status`, `send_time`) VALUES
(1, 1, 'EMAIL', '明天是张三的生日，记得准备礼物哦！', 1, '2024-01-15 09:00:00'),
(1, 2, 'EMAIL', '还有3天就是李四的生日了，提前准备一下吧！', 1, '2024-01-17 09:00:00'),
(1, 1, 'EMAIL', '一周后是张三的生日，可以开始计划庆祝活动了！', 0, '2024-01-08 09:00:00'),
(2, 4, 'EMAIL', '明天是赵六的生日，别忘了送祝福！', 1, '2024-01-24 09:00:00');

-- ----------------------------
-- 创建索引优化查询性能
-- ----------------------------

-- 用户表索引
CREATE INDEX `idx_user_email_status` ON `user` (`email`, `status`);

-- 生日角色表索引
CREATE INDEX `idx_birthday_role_user_birthday` ON `birthday_role` (`user_id`, `birthday`);
CREATE INDEX `idx_birthday_role_user_lunar` ON `birthday_role` (`user_id`, `lunar_birthday`);

-- 提醒设置表索引
CREATE INDEX `idx_reminder_user_role` ON `reminder` (`user_id`, `role_id`);

-- 通知日志表索引
CREATE INDEX `idx_notice_log_user_time` ON `notice_log` (`user_id`, `send_time` DESC);
CREATE INDEX `idx_notice_log_role_time` ON `notice_log` (`role_id`, `send_time` DESC);

-- ----------------------------
-- 创建视图简化查询
-- ----------------------------

-- 用户生日角色统计视图
CREATE VIEW `v_user_role_stats` AS
SELECT 
    u.id AS user_id,
    u.email,
    u.nickname,
    COUNT(br.id) AS role_count,
    COUNT(CASE WHEN br.is_lunar = 0 THEN 1 END) AS gregorian_count,
    COUNT(CASE WHEN br.is_lunar = 1 THEN 1 END) AS lunar_count
FROM `user` u
LEFT JOIN `birthday_role` br ON u.id = br.user_id
WHERE u.status = 1
GROUP BY u.id, u.email, u.nickname;

-- 今日生日视图
CREATE VIEW `v_today_birthdays` AS
SELECT 
    br.id,
    br.user_id,
    br.name,
    br.birthday,
    br.lunar_birthday,
    br.is_lunar,
    br.remark,
    u.email AS user_email,
    u.nickname AS user_nickname
FROM `birthday_role` br
JOIN `user` u ON br.user_id = u.id
WHERE u.status = 1
  AND (
    (br.is_lunar = 0 AND DATE_FORMAT(br.birthday, '%m-%d') = DATE_FORMAT(CURDATE(), '%m-%d'))
    OR
    (br.is_lunar = 1 AND br.lunar_birthday IS NOT NULL AND DATE_FORMAT(br.lunar_birthday, '%m-%d') = DATE_FORMAT(CURDATE(), '%m-%d'))
  );

-- 即将到来的生日视图（未来30天）
CREATE VIEW `v_upcoming_birthdays` AS
SELECT 
    br.id,
    br.user_id,
    br.name,
    br.birthday,
    br.lunar_birthday,
    br.is_lunar,
    br.remark,
    u.email AS user_email,
    u.nickname AS user_nickname,
    CASE 
        WHEN br.is_lunar = 0 THEN
            CASE 
                WHEN DATE_FORMAT(br.birthday, '%m-%d') >= DATE_FORMAT(CURDATE(), '%m-%d') THEN
                    DATEDIFF(DATE(CONCAT(YEAR(CURDATE()), '-', DATE_FORMAT(br.birthday, '%m-%d'))), CURDATE())
                ELSE
                    DATEDIFF(DATE(CONCAT(YEAR(CURDATE()) + 1, '-', DATE_FORMAT(br.birthday, '%m-%d'))), CURDATE())
            END
        ELSE
            CASE 
                WHEN br.lunar_birthday IS NOT NULL AND DATE_FORMAT(br.lunar_birthday, '%m-%d') >= DATE_FORMAT(CURDATE(), '%m-%d') THEN
                    DATEDIFF(DATE(CONCAT(YEAR(CURDATE()), '-', DATE_FORMAT(br.lunar_birthday, '%m-%d'))), CURDATE())
                WHEN br.lunar_birthday IS NOT NULL THEN
                    DATEDIFF(DATE(CONCAT(YEAR(CURDATE()) + 1, '-', DATE_FORMAT(br.lunar_birthday, '%m-%d'))), CURDATE())
                ELSE 999
            END
    END AS days_until_birthday
FROM `birthday_role` br
JOIN `user` u ON br.user_id = u.id
WHERE u.status = 1
HAVING days_until_birthday BETWEEN 0 AND 30
ORDER BY days_until_birthday ASC;

-- ----------------------------
-- 创建存储过程
-- ----------------------------

-- 获取用户生日提醒列表的存储过程
DELIMITER $$
CREATE PROCEDURE `GetUserBirthdayReminders`(
    IN p_user_id BIGINT,
    IN p_advance_days INT
)
BEGIN
    SELECT DISTINCT
        br.id AS role_id,
        br.name,
        br.birthday,
        br.lunar_birthday,
        br.is_lunar,
        br.remark,
        r.notify_type,
        r.advance_days,
        u.email AS user_email
    FROM `birthday_role` br
    JOIN `user` u ON br.user_id = u.id
    JOIN `reminder` r ON br.id = r.role_id AND br.user_id = r.user_id
    WHERE u.status = 1
      AND (p_user_id IS NULL OR br.user_id = p_user_id)
      AND (
        -- 公历生日检查
        (br.is_lunar = 0 AND 
         DATEDIFF(
           CASE 
             WHEN DATE_FORMAT(br.birthday, '%m-%d') >= DATE_FORMAT(CURDATE(), '%m-%d') THEN
               DATE(CONCAT(YEAR(CURDATE()), '-', DATE_FORMAT(br.birthday, '%m-%d')))
             ELSE
               DATE(CONCAT(YEAR(CURDATE()) + 1, '-', DATE_FORMAT(br.birthday, '%m-%d')))
           END,
           CURDATE()
         ) = COALESCE(p_advance_days, r.advance_days))
        OR
        -- 农历生日检查
        (br.is_lunar = 1 AND br.lunar_birthday IS NOT NULL AND
         DATEDIFF(
           CASE 
             WHEN DATE_FORMAT(br.lunar_birthday, '%m-%d') >= DATE_FORMAT(CURDATE(), '%m-%d') THEN
               DATE(CONCAT(YEAR(CURDATE()), '-', DATE_FORMAT(br.lunar_birthday, '%m-%d')))
             ELSE
               DATE(CONCAT(YEAR(CURDATE()) + 1, '-', DATE_FORMAT(br.lunar_birthday, '%m-%d')))
           END,
           CURDATE()
         ) = COALESCE(p_advance_days, r.advance_days))
      );
END$$
DELIMITER ;

-- ----------------------------
-- 创建触发器
-- ----------------------------

-- 用户删除时清理相关数据的触发器
DELIMITER $$
CREATE TRIGGER `tr_user_delete_cleanup`
AFTER DELETE ON `user`
FOR EACH ROW
BEGIN
    -- 删除相关的通知日志（如果外键约束没有级联删除）
    DELETE FROM `notice_log` WHERE `user_id` = OLD.id;
    
    -- 记录删除日志（可选，需要先创建日志表）
    -- INSERT INTO `system_log` (`action`, `table_name`, `record_id`, `create_time`) 
    -- VALUES ('DELETE', 'user', OLD.id, NOW());
END$$
DELIMITER ;

-- 生日角色更新时的触发器
DELIMITER $$
CREATE TRIGGER `tr_birthday_role_update`
BEFORE UPDATE ON `birthday_role`
FOR EACH ROW
BEGIN
    -- 确保更新时间字段被正确设置
    SET NEW.update_time = CURRENT_TIMESTAMP;
    
    -- 如果是农历生日，可以在这里添加农历转公历的逻辑
    -- 这里只是示例，实际需要根据农历转换算法实现
    IF NEW.is_lunar = 1 AND NEW.lunar_birthday IS NULL THEN
        SET NEW.lunar_birthday = NEW.birthday;
    END IF;
END$$
DELIMITER ;

-- ----------------------------
-- 设置权限和完成初始化
-- ----------------------------

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 提交事务
COMMIT;

-- 显示初始化完成信息
SELECT '数据库初始化完成！' AS message,
       (SELECT COUNT(*) FROM `user`) AS user_count,
       (SELECT COUNT(*) FROM `birthday_role`) AS role_count,
       (SELECT COUNT(*) FROM `reminder`) AS reminder_count,
       (SELECT COUNT(*) FROM `notice_log`) AS log_count;