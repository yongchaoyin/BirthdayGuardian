# 登录问题诊断和解决方案

## 已添加的调试功能

### 1. 前端调试（浏览器控制台）
- 登录请求数据
- 登录响应数据
- 详细错误信息

### 2. 后端调试（控制台输出）
- 登录账号
- 找到的用户信息
- 密码MD5对比
- Token生成信息

## 诊断步骤

### 步骤1：运行诊断脚本

```bash
cd /Users/yongchao/Code/BirthdayGuardian-1
./diagnose-login.sh
```

这将自动测试：
- 后端服务状态
- 数据库连接
- 注册接口
- 登录接口

### 步骤2：重启服务

**本地开发模式：**

```bash
# 终端1 - 启动后端
cd /Users/yongchao/Code/BirthdayGuardian-1/backend
mvn spring-boot:run

# 终端2 - 启动前端
cd /Users/yongchao/Code/BirthdayGuardian-1/frontend
npm run dev
```

**Docker模式：**

```bash
cd /Users/yongchao/Code/BirthdayGuardian-1
docker-compose down
docker-compose up -d --build
```

### 步骤3：测试登录

1. **打开浏览器**
   - 本地开发: http://localhost:5173
   - Docker: http://localhost

2. **打开开发者工具（F12）**
   - 切换到 Console 标签
   - 切换到 Network 标签

3. **先注册新用户**
   - 点击"立即注册"
   - 填写信息（用户名、邮箱、密码）
   - 查看控制台输出
   - 查看Network面板的请求

4. **使用刚注册的账号登录**
   - 输入用户名或邮箱
   - 输入密码
   - 点击登录
   - **查看控制台输出的调试信息**

## 常见错误和解决方案

### 错误1: 网络错误

**现象**: 提示"网络错误，请检查后端服务"

**解决方案**:
```bash
# 检查后端是否启动
curl http://localhost:8089/api/auth/login

# 如未启动，启动后端
cd backend && mvn spring-boot:run
```

### 错误2: 用户不存在

**现象**: 提示"用户不存在"

**后端日志**:
```
登录请求 - 账号: testuser
登录失败 - 用户不存在: testuser
```

**解决方案**:
- 先注册该用户
- 检查输入的用户名/邮箱是否正确

### 错误3: 密码错误

**现象**: 提示"密码错误"

**后端日志**:
```
登录请求 - 账号: testuser
找到用户 - ID: 1, 用户名: testuser
密码MD5: e10adc3949ba59abbe56e057f20f883e
数据库密码: 不同的MD5
登录失败 - 密码错误
```

**解决方案**:
- 确认密码输入正确
- 如忘记密码，重新注册一个新账号测试

### 错误4: 未获取到token

**现象**: 提示"登录失败：未获取到token"

**前端控制台**:
```javascript
登录响应: { code: 200, data: { token: "xxx", userId: 1, ... } }
```

**解决方案**:
- 检查响应数据结构
- 确认token字段存在

### 错误5: 跨域问题

**现象**: Network显示CORS错误

**解决方案**:
后端已配置CORS，如仍有问题：

```java
// CorsConfig.java 已配置
.allowedOrigins("http://localhost:5173", "http://localhost:3000")
```

## 查看详细日志

### 前端日志
1. 打开浏览器 F12
2. 切换到 Console 标签
3. 看到的日志：
   - `发送请求:` - 请求信息
   - `收到响应:` - 响应信息
   - `登录错误:` - 错误详情

### 后端日志

**本地开发**:
查看控制台输出

**Docker部署**:
```bash
docker-compose logs -f backend
```

看到的日志：
```
登录请求 - 账号: testuser
找到用户 - ID: 1, 用户名: testuser
密码MD5: e10adc3949ba59abbe56e057f20f883e
数据库密码: e10adc3949ba59abbe56e057f20f883e
密码验证成功，生成Token
登录成功 - Token: eyJhbGciOiJIUzUxMiJ9...
```

## 测试用例

### 快速测试

```bash
# 1. 注册测试用户
curl -X POST http://localhost:8089/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test001","email":"test001@example.com","password":"123456"}'

# 期望响应: {"code":200,"message":"操作成功","data":null}

# 2. 登录测试
curl -X POST http://localhost:8089/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"test001","password":"123456"}'

# 期望响应: {"code":200,"message":"操作成功","data":{"token":"xxx","userId":1,"username":"test001","email":"test001@example.com"}}
```

## 问题排查检查清单

- [ ] 后端服务已启动（端口8089）
- [ ] 前端服务已启动（端口5173或80）
- [ ] MySQL已运行
- [ ] 用户已注册
- [ ] 用户名/邮箱正确
- [ ] 密码正确
- [ ] 浏览器控制台无错误
- [ ] 后端日志无错误

## 如果问题仍未解决

请提供以下信息：

1. **前端控制台完整日志**（F12 → Console）
2. **后端控制台日志**
3. **Network面板的请求详情**（F12 → Network）
4. **具体错误提示**

复制这些信息可以帮助快速定位问题。
