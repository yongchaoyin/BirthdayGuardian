#!/bin/bash

# 数据库迁移脚本 - 添加role字段和announcement表

echo "开始执行数据库迁移..."

# 检查MySQL容器是否运行
if ! docker ps | grep -q birthday-guardian-mysql; then
    echo "MySQL容器未运行,请先启动docker-compose"
    exit 1
fi

# 执行迁移SQL
docker exec -i birthday-guardian-mysql mysql -uroot -proot birthday_guardian <<EOF
-- 添加role字段到user表
ALTER TABLE user ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色:admin-管理员,user-普通用户' AFTER password;

-- 创建公告表
CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(200) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  create_user_id BIGINT NOT NULL COMMENT '创建人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 创建admin用户 (密码: admin@123)
-- 注意: 这里使用MD5加密的密码
INSERT INTO user (username, email, password, role)
VALUES ('admin', 'admin@example.com', '0192023a7bbd73250516f069df18b500', 'admin')
ON DUPLICATE KEY UPDATE role = 'admin';

SELECT 'Migration completed successfully!' as status;
EOF

if [ $? -eq 0 ]; then
    echo "数据库迁移完成!"
else
    echo "数据库迁移失败,请检查错误信息"
    exit 1
fi
