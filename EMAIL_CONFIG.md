# 邮件配置说明

生日守护者系统支持多种邮箱服务商，推荐使用163邮箱（国内访问稳定）。

## 支持的邮箱服务

### 1. 163邮箱（推荐）

**配置信息**:
- SMTP服务器: `smtp.163.com`
- 端口: `465` (SSL)
- 需要授权码（不是邮箱密码）

**获取授权码步骤**:
1. 登录163邮箱
2. 进入设置 → POP3/SMTP/IMAP
3. 开启"SMTP服务"
4. 获取授权码
5. 将授权码填入配置文件

**配置示例**:
```bash
SPRING_MAIL_HOST=smtp.163.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=your-email@163.com
SPRING_MAIL_PASSWORD=your-authorization-code
```

### 2. Gmail

**配置信息**:
- SMTP服务器: `smtp.gmail.com`
- 端口: `587` (STARTTLS)
- 需要应用专用密码

**获取应用专用密码**:
1. 登录Gmail账户
2. 开启两步验证
3. 生成应用专用密码
4. 将密码填入配置文件

**配置示例**:
```bash
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```

### 3. QQ邮箱

**配置信息**:
- SMTP服务器: `smtp.qq.com`
- 端口: `465` (SSL)
- 需要授权码

**获取授权码步骤**:
1. 登录QQ邮箱
2. 进入设置 → 账户
3. 开启"POP3/SMTP服务"
4. 获取授权码
5. 将授权码填入配置文件

**配置示例**:
```bash
SPRING_MAIL_HOST=smtp.qq.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=your-email@qq.com
SPRING_MAIL_PASSWORD=your-authorization-code
```

### 4. 126邮箱

**配置信息**:
- SMTP服务器: `smtp.126.com`
- 端口: `465` (SSL)
- 需要授权码

**配置示例**:
```bash
SPRING_MAIL_HOST=smtp.126.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=your-email@126.com
SPRING_MAIL_PASSWORD=your-authorization-code
```

## 配置步骤

### Docker部署

1. 编辑 `.env` 文件:
```bash
cp .env.example .env
vim .env
```

2. 修改邮件配置:
```bash
SPRING_MAIL_HOST=smtp.163.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=your-email@163.com
SPRING_MAIL_PASSWORD=your-authorization-code
```

3. 重启服务:
```bash
docker-compose restart backend
```

### 本地开发

编辑 `backend/src/main/resources/application.yml`:
```yaml
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: your-email@163.com
    password: your-authorization-code
```

## 测试邮件发送

### 1. 通过API测试

登录后调用测试接口：
```bash
curl -X POST http://localhost:8080/api/test/email \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"to":"test@example.com"}'
```

### 2. 查看日志

Docker部署:
```bash
docker-compose logs -f backend
```

本地部署:
查看控制台输出

## 常见问题

### 1. 邮件发送失败

**可能原因**:
- 授权码错误
- SMTP服务未开启
- 防火墙阻止
- 邮箱登录保护

**解决方案**:
- 重新获取授权码
- 确认开启SMTP服务
- 检查防火墙设置
- 关闭邮箱的"登录保护"功能

### 2. 连接超时

**可能原因**:
- 网络问题
- 端口被阻止
- 服务器地址错误

**解决方案**:
- 检查网络连接
- 尝试使用不同的端口（465或587）
- 确认SMTP服务器地址正确

### 3. 认证失败

**可能原因**:
- 使用了邮箱密码而非授权码
- 授权码过期
- 用户名格式错误

**解决方案**:
- 确保使用授权码，不是登录密码
- 重新生成授权码
- 确认用户名包含完整邮箱地址

## 端口说明

- **465端口**: 使用SSL加密，连接即加密
- **587端口**: 使用STARTTLS，先连接后加密

推荐使用465端口（SSL），更加安全稳定。

## 安全建议

1. **不要在代码中硬编码邮箱密码**
2. **使用环境变量管理敏感信息**
3. **定期更换授权码**
4. **限制发送频率，避免被标记为垃圾邮件**
5. **生产环境使用企业邮箱**

## 技术细节

系统使用 `javax.mail` 库发送邮件，支持：
- SSL/TLS加密
- SMTP身份认证
- 自动重试机制
- 异常日志记录

邮件服务在应用启动时自动初始化，无需手动配置。
