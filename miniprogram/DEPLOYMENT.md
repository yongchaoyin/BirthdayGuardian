# 生日守护者小程序部署指南

本文档详细说明如何部署生日守护者小程序到生产环境。

## 部署前准备

### 1. 微信小程序账号

1. 注册微信小程序账号：https://mp.weixin.qq.com/
2. 完成主体认证（个人或企业）
3. 获取小程序 AppID 和 AppSecret

### 2. 微信公众号（可选）

如需公众号推送功能：
1. 注册微信公众号（服务号）：https://mp.weixin.qq.com/
2. 完成主体认证
3. 开通模板消息功能
4. 获取公众号 AppID 和 AppSecret

### 3. 服务器准备

- 服务器（支持 Docker 或直接运行 Java）
- 域名并完成 ICP 备案
- SSL 证书（小程序要求 HTTPS）
- MySQL 数据库

## 后端部署

### 1. 配置环境变量

创建 `.env` 文件或设置系统环境变量：

```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/birthday_guardian
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your-password

# 邮件配置
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# 微信小程序配置
WECHAT_MINIAPP_APP_ID=wx1234567890abcdef
WECHAT_MINIAPP_SECRET=your-miniapp-secret

# 微信公众号配置（可选）
WECHAT_MP_APP_ID=wx0987654321fedcba
WECHAT_MP_SECRET=your-mp-secret
WECHAT_MP_TOKEN=your-token
WECHAT_MP_AES_KEY=your-aes-key
WECHAT_MP_BIRTHDAY_TEMPLATE_ID=your-template-id
WECHAT_MP_REMINDER_URL=https://your-domain.com/reminder

# JWT配置
JWT_SECRET=birthdayGuardianSecretKeyForHS512AlgorithmMustBeAtLeast64BytesLong2024
JWT_EXPIRATION=86400000

# 短信配置（可选）
SMS_SUPPLIER=alibaba
SMS_ACCESS_KEY=your-access-key
SMS_ACCESS_SECRET=your-access-secret
SMS_TEMPLATE_ID=your-template-id
SMS_SIGN_NAME=生日守护者

# 定时任务配置
SCHEDULE_CHECK_TIME=0 0 9 * * ?

# 时区
TZ=Asia/Shanghai
```

### 2. Docker 部署（推荐）

```bash
# 克隆代码
git clone https://github.com/your-repo/BirthdayGuardian.git
cd BirthdayGuardian

# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f backend
```

### 3. 直接部署

```bash
# 进入后端目录
cd backend

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/birthday-guardian.jar
```

### 4. 配置 Nginx 反向代理

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/your/certificate.crt;
    ssl_certificate_key /path/to/your/private.key;

    location /api {
        proxy_pass http://localhost:8089;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

重启 Nginx：
```bash
nginx -t
nginx -s reload
```

### 5. 验证后端部署

```bash
# 测试健康检查
curl https://your-domain.com/api/test/health

# 应返回: {"code":200,"message":"Success","data":"Hello from Birthday Guardian!"}
```

## 小程序部署

### 1. 修改配置

#### 修改 `app.js`

```javascript
globalData: {
  userInfo: null,
  isLogin: false,
  apiBase: 'https://your-domain.com' // 修改为实际域名
}
```

### 2. 配置小程序后台

登录微信小程序后台：https://mp.weixin.qq.com/

#### 配置服务器域名

**开发管理 > 开发设置 > 服务器域名**

- **request合法域名**：`https://your-domain.com`
- **uploadFile合法域名**：`https://your-domain.com`（如有上传需求）
- **downloadFile合法域名**：`https://your-domain.com`（如有下载需求）

**注意**：
- 必须使用 HTTPS
- 域名必须 ICP 备案
- 域名不能使用 IP 地址
- 域名不能带端口号

### 3. 准备图片资源

将以下图片放入 `images/` 目录：
- `tab-home.png` / `tab-home-active.png`
- `tab-birthday.png` / `tab-birthday-active.png`
- `tab-profile.png` / `tab-profile-active.png`
- `default-avatar.png`

### 4. 上传代码

1. 打开微信开发者工具
2. 导入小程序项目
3. 在工具栏点击"上传"
4. 填写版本号和项目备注
5. 上传成功

### 5. 提交审核

1. 登录小程序后台
2. 进入"版本管理"
3. 找到刚上传的版本
4. 点击"提交审核"
5. 填写审核信息：
   - 功能页面：选择首页、生日管理、个人中心
   - 测试账号：提供测试用的微信号
   - 配置：核心功能、业务域名等

### 6. 发布上线

审核通过后（通常 1-3 个工作日）：
1. 进入"版本管理"
2. 点击"发布"按钮
3. 小程序即可在微信中被搜索和使用

## 公众号配置（可选）

### 1. 配置服务器

**开发 > 基本配置 > 服务器配置**

- **URL**：`https://your-domain.com/api/wechat/portal`
- **Token**：与环境变量 `WECHAT_MP_TOKEN` 一致
- **EncodingAESKey**：与环境变量 `WECHAT_MP_AES_KEY` 一致
- **消息加解密方式**：安全模式（推荐）

### 2. 配置网页授权域名

**设置与开发 > 公众号设置 > 功能设置 > 网页授权域名**

添加：`your-domain.com`

### 3. 申请模板消息

**功能 > 模板消息**

搜索并添加"生日提醒"类模板，记录模板 ID。

模板示例：
```
标题：生日提醒
first：{{first.DATA}}
角色姓名：{{keyword1.DATA}}
生日日期：{{keyword2.DATA}}
remark：{{remark.DATA}}
```

### 4. 用户绑定流程

用户需要通过以下方式绑定：

1. **方式一：公众号菜单**
   - 设置自定义菜单
   - 跳转到 H5 授权页面
   - 授权后调用绑定接口

2. **方式二：小程序跳转**
   - 在小程序中引导关注公众号
   - 通过公众号消息跳转绑定

## 监控与维护

### 1. 日志查看

```bash
# Docker 部署
docker-compose logs -f backend

# 直接部署
tail -f /path/to/logs/birthday-guardian.log
```

### 2. 数据库备份

```bash
# 每日自动备份
0 2 * * * docker-compose exec -T mysql mysqldump -uroot -proot birthday_guardian > /backup/birthday_guardian_$(date +\%Y\%m\%d).sql
```

### 3. 性能监控

- 监控服务器 CPU、内存、磁盘使用率
- 监控数据库连接数和慢查询
- 监控 API 响应时间
- 设置告警通知

### 4. 定期更新

```bash
# 拉取最新代码
git pull origin main

# 重新构建并部署
docker-compose down
docker-compose up -d --build
```

## 常见问题

### 1. 小程序无法访问接口

**原因**：
- 域名未在小程序后台配置
- HTTPS 证书问题
- CORS 配置问题

**解决**：
- 检查小程序后台的服务器域名配置
- 确保 SSL 证书有效
- 检查后端 CORS 配置

### 2. 用户无法登录

**原因**：
- 小程序 AppID/Secret 配置错误
- 网络问题
- 后端服务未启动

**解决**：
- 检查后端配置的小程序 AppID 和 Secret
- 测试后端 API 是否正常
- 查看后端日志

### 3. 公众号推送失败

**原因**：
- 用户未绑定公众号
- 模板 ID 配置错误
- AccessToken 过期

**解决**：
- 确认用户已完成绑定
- 检查模板 ID 配置
- 查看后端日志中的微信 API 错误信息

### 4. 定时任务未执行

**原因**：
- Cron 表达式配置错误
- 服务器时区问题

**解决**：
- 检查 `SCHEDULE_CHECK_TIME` 配置
- 确认服务器时区设置为 `Asia/Shanghai`
- 查看定时任务日志

## 安全建议

1. **数据库安全**
   - 使用强密码
   - 限制远程访问
   - 定期备份

2. **API 安全**
   - 启用 HTTPS
   - 实现请求限流
   - 添加请求签名

3. **密钥管理**
   - 不要将密钥提交到代码仓库
   - 使用环境变量或密钥管理服务
   - 定期更换密钥

4. **日志管理**
   - 不要记录敏感信息
   - 定期清理旧日志
   - 监控异常日志

## 技术支持

如需帮助，请联系：
- 邮箱：yinyc0925@outlook.com
- 提交 Issue：GitHub Issues

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现微信小程序登录
- 实现生日管理功能
- 支持公众号推送
