# 生日守护者小程序

生日守护者微信小程序端，提供便捷的生日管理功能，让您随时随地守护重要的日子。

## 功能特性

### 用户功能
- ✅ 微信一键登录（自动创建账户）
- ✅ 首页展示即将到来的生日
- ✅ 生日管理（增删改查）
- ✅ 支持阳历和阴历生日
- ✅ 自定义提前提醒天数
- ✅ 个人中心（修改用户名、邮箱、手机号）
- ✅ 会员信息展示（守护名额、VIP到期时间）

### 会员制度
- 温馨体验：可守护 3 位亲友
- VIP守护礼遇：可守护 20 位亲友，生日当天短信祝福

## 技术栈

- 微信小程序原生框架
- Promise 异步处理
- 模块化开发

## 目录结构

```
miniprogram/
├── api/                    # API接口封装
│   ├── auth.js            # 认证相关API
│   └── birthday.js        # 生日管理API
├── images/                # 图片资源
│   └── README.md          # 图片说明文档
├── pages/                 # 页面
│   ├── index/            # 首页
│   ├── login/            # 登录页
│   ├── birthdays/        # 生日管理
│   │   ├── list.*        # 生日列表
│   │   └── edit.*        # 添加/编辑生日
│   └── profile/          # 个人中心
├── utils/                # 工具函数
│   ├── request.js       # 网络请求封装
│   └── util.js          # 通用工具函数
├── app.js               # 小程序入口
├── app.json             # 小程序配置
├── app.wxss             # 全局样式
├── project.config.json  # 项目配置
└── sitemap.json         # 搜索配置
```

## 开发指南

### 前置要求

1. 微信开发者工具
2. 微信小程序账号
3. 后端服务已启动

### 配置步骤

#### 1. 配置小程序 AppID

在 `project.config.json` 中配置您的小程序 AppID：

```json
{
  "appid": "your-miniprogram-appid"
}
```

#### 2. 配置后端 API 地址

在 `app.js` 中修改 API 基础地址：

```javascript
globalData: {
  apiBase: 'http://your-backend-domain:8089' // 修改为实际后端地址
}
```

开发环境：`http://localhost:8089`
生产环境：`https://your-domain.com`

#### 3. 配置后端小程序参数

在后端 `application.yml` 中配置小程序信息：

```yaml
wechat:
  miniapp:
    app-id: ${WECHAT_MINIAPP_APP_ID:your-miniapp-appid}
    secret: ${WECHAT_MINIAPP_SECRET:your-miniapp-secret}
```

或使用环境变量：

```bash
WECHAT_MINIAPP_APP_ID=your-miniapp-appid
WECHAT_MINIAPP_SECRET=your-miniapp-secret
```

#### 4. 准备图片资源

将所需的图标文件放入 `images/` 目录：
- TabBar 图标（首页、生日管理、个人中心）
- 默认头像

详见 [images/README.md](images/README.md)

#### 5. 配置服务器域名

在微信小程序后台配置合法域名：

**开发管理 > 开发设置 > 服务器域名**

- request合法域名：`https://your-domain.com`

**注意：**开发阶段可以在微信开发者工具中勾选"不校验合法域名"。

### 本地开发

1. 使用微信开发者工具打开 `miniprogram` 目录
2. 确保后端服务已启动（默认端口 8089）
3. 点击"编译"开始调试

### 功能测试

#### 1. 登录流程测试
- 打开小程序，点击"微信一键登录"
- 授权后自动创建账户并跳转首页

#### 2. 添加生日测试
- 进入"生日管理"页面
- 点击"添加生日提醒"
- 填写角色信息并保存

#### 3. 编辑/删除测试
- 在生日列表中点击"编辑"或"删除"
- 验证功能是否正常

## API 接口说明

### 认证接口

#### 小程序登录
```
POST /api/auth/wx-miniprogram-login
Body: { "code": "微信登录code" }
Response: { "token": "jwt-token", "userId": 1, "username": "...", ... }
```

#### 获取用户信息
```
GET /api/user/info
Headers: Authorization: Bearer {token}
Response: { "username": "...", "email": "...", ... }
```

### 生日管理接口

#### 获取生日列表
```
GET /api/roles
Headers: Authorization: Bearer {token}
Response: {
  "roles": [...],
  "membershipLevel": "FREE/VIP",
  "maxRoleCount": 3,
  "currentCount": 1,
  "remainingSlots": 2
}
```

#### 添加生日
```
POST /api/roles
Headers: Authorization: Bearer {token}
Body: {
  "roleType": "父亲",
  "roleName": "爸爸",
  "birthDate": "1970-01-01",
  "calendarType": 1,  // 1-阳历, 2-阴历
  "remindDays": 3,
  "rolePhone": "13800138000",
  "remark": "备注"
}
```

#### 更新生日
```
PUT /api/roles/{id}
Headers: Authorization: Bearer {token}
Body: { ... }
```

#### 删除生日
```
DELETE /api/roles/{id}
Headers: Authorization: Bearer {token}
```

## 公众号集成

### 生日提醒通知

当用户关注公众号并绑定账号后，系统会在生日到来前通过公众号模板消息推送提醒。

#### 配置步骤

1. **配置公众号参数**

在后端 `application.yml` 中配置：

```yaml
wechat:
  mp:
    app-id: ${WECHAT_MP_APP_ID:your-mp-appid}
    secret: ${WECHAT_MP_SECRET:your-mp-secret}
    token: ${WECHAT_MP_TOKEN:your-token}
    aes-key: ${WECHAT_MP_AES_KEY:your-aes-key}
    birthday-template-id: ${WECHAT_MP_BIRTHDAY_TEMPLATE_ID:template-id}
    reminder-url: ${WECHAT_MP_REMINDER_URL:https://your-h5-url}
```

2. **申请模板消息**

在微信公众平台申请生日提醒模板消息，包含以下字段：
- first：提醒文案
- keyword1：角色姓名
- keyword2：生日日期
- remark：备注信息

3. **用户绑定流程**

用户通过公众号菜单或H5页面进行OAuth授权，授权后调用后端绑定接口：

```
POST /api/wechat/bind
Headers: Authorization: Bearer {token}
Body: { "code": "oauth-code" }
```

### 推送时机

系统每天定时检查（默认上午9点），对于符合提醒条件的生日，会依次通过以下渠道通知：
1. 邮件提醒
2. 微信公众号模板消息（如已绑定）
3. 短信祝福（VIP专享，生日当天）

## 注意事项

1. **登录凭证**：小程序使用 JWT token 进行身份认证，token 有效期 24 小时
2. **数据安全**：所有 API 请求均需要携带 token（登录接口除外）
3. **会员限制**：
   - 免费用户最多添加 3 个生日
   - VIP 用户最多添加 20 个生日
4. **网络请求**：
   - 开发环境使用 HTTP
   - 生产环境必须使用 HTTPS
5. **图片资源**：需要自行准备 TabBar 图标和头像图片

## 常见问题

### 1. 登录失败

**问题**：点击登录后提示"登录失败"

**解决方案**：
- 检查后端服务是否正常运行
- 检查 `app.js` 中的 API 地址是否正确
- 检查后端是否配置了小程序 AppID 和 Secret
- 查看微信开发者工具控制台的错误信息

### 2. 网络请求失败

**问题**：所有 API 请求都失败

**解决方案**：
- 开发阶段：勾选"不校验合法域名"
- 生产环境：在小程序后台配置服务器域名白名单
- 检查后端是否允许跨域（CORS配置）

### 3. 图片不显示

**问题**：TabBar 图标或头像不显示

**解决方案**：
- 确保图片文件已放入 `images/` 目录
- 检查文件名是否与代码中引用的一致
- 使用相对路径引用图片

### 4. 添加生日失败

**问题**：点击保存后提示"添加失败"

**解决方案**：
- 检查是否已达到守护名额上限
- 确保必填字段都已填写
- 检查日期格式是否正确
- 查看后端日志获取详细错误信息

## 后续优化建议

1. **性能优化**
   - 实现列表虚拟滚动
   - 添加数据缓存机制
   - 图片懒加载

2. **用户体验**
   - 添加骨架屏加载
   - 优化动画效果
   - 添加空状态引导

3. **功能扩展**
   - 支持批量导入生日
   - 添加生日统计图表
   - 实现生日分享功能
   - 添加生日祝福语库

4. **安全增强**
   - 实现 token 自动刷新
   - 添加请求签名
   - 敏感信息加密存储

## 技术支持

如遇到问题，请联系：
- 邮箱：yinyc0925@outlook.com
- GitHub：提交 Issue

## License

MIT
