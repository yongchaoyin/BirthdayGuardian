# Docker部署指南

## 快速开始

### 1. 配置环境变量

```bash
# 复制配置文件模板
cp .env.example .env

# 编辑.env文件
vim .env  # 或使用其他编辑器
```

必须修改的配置：
- `SPRING_MAIL_USERNAME`: 你的邮箱地址
- `SPRING_MAIL_PASSWORD`: 你的邮箱授权码

### 2. 启动服务

```bash
# 使用启动脚本（推荐）
./docker-start.sh

# 或直接使用docker-compose
docker-compose up -d
```

### 3. 访问应用

- 前端: http://localhost
- 后端API: http://localhost:8080

## Docker命令

### 基本操作

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 构建和清理

```bash
# 重新构建镜像
docker-compose build --no-cache

# 停止并删除容器、网络
docker-compose down

# 停止并删除容器、网络、数据卷
docker-compose down -v

# 停止并删除容器、网络、镜像
docker-compose down --rmi all
```

### 进入容器

```bash
# 进入后端容器
docker-compose exec backend bash

# 进入前端容器
docker-compose exec frontend sh

# 进入MySQL容器
docker-compose exec mysql bash
```

## 数据持久化

数据存储在Docker数据卷 `birthday-guardian_mysql-data` 中，即使删除容器数据也不会丢失。

### 数据备份

```bash
# 备份MySQL数据
docker-compose exec mysql mysqldump -uroot -proot birthday_guardian > backup_$(date +%Y%m%d).sql
```

### 数据恢复

```bash
# 恢复MySQL数据
docker-compose exec -T mysql mysql -uroot -proot birthday_guardian < backup.sql
```

## 常见问题

### MySQL启动失败

如果MySQL服务无法启动，可能是因为：
1. 端口3306已被占用
2. 数据卷损坏

解决方案：

```bash
# 停止所有服务
docker-compose down

# 删除数据卷（注意：会删除所有数据）
docker volume rm birthdayguardian-1_mysql-data

# 重新启动
docker-compose up -d
```

### 后端连接不上MySQL

后端启动时需要等待MySQL完全就绪。如果出现连接错误：

```bash
# 查看MySQL状态
docker-compose ps

# 重启后端
docker-compose restart backend
```

### 修改配置后不生效

修改配置文件后需要重新构建镜像：

```bash
# 重新构建并启动
docker-compose up -d --build
```

## 端口配置

默认端口：
- 前端: 80
- 后端: 8080
- MySQL: 3306

修改端口请编辑 `docker-compose.yml` 文件。

## 生产环境建议

1. **安全配置**:
   - 修改MySQL默认密码
   - 使用环境变量管理敏感信息
   - 配置防火墙规则

2. **性能优化**:
   - 配置MySQL参数
   - 调整JVM参数
   - 使用Nginx缓存

3. **监控和日志**:
   - 配置日志收集
   - 设置告警规则
   - 定期备份数据

4. **HTTPS配置**:
   - 使用Let's Encrypt证书
   - 配置Nginx SSL
   - 强制HTTPS重定向
