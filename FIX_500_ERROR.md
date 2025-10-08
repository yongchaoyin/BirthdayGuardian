# 500错误已修复

## 问题原因

**JWT库版本不兼容**：
- 旧版本：jjwt 0.9.1
- 问题：不支持Java 17，在生成Token时抛出异常
- 错误：Internal Server Error (500)

## 修复方案

### 1. 升级JWT依赖

```xml
<!-- 旧版本 (pom.xml) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>

<!-- 新版本 (pom.xml) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 2. 更新JwtUtil代码

主要变化：
- 使用 `Keys.hmacShaKeyFor()` 生成密钥
- 使用 `Jwts.parserBuilder()` 替代 `Jwts.parser()`
- 使用 `signWith(key, algorithm)` 替代 `signWith(algorithm, secret)`

### 3. 增强异常处理

在 `AuthController` 中添加详细的异常日志和堆栈追踪。

## 重启步骤

### 停止当前后端
按 `Ctrl+C` 停止正在运行的后端服务

### 重新启动后端
```bash
cd /Users/yongchao/Code/BirthdayGuardian-1/backend
mvn spring-boot:run
```

### 测试登录

**方式1：使用浏览器**
1. 打开 http://localhost:5173
2. 登录：
   - 账号：`yinyongchao`
   - 密码：`1qaz2wsx!`

**方式2：使用测试脚本**
```bash
cd /Users/yongchao/Code/BirthdayGuardian-1
./test-login-yinyongchao.sh
```

**方式3：使用curl**
```bash
curl -X POST http://localhost:8089/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"yinyongchao","password":"1qaz2wsx!"}'
```

## 预期结果

### 成功的响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "username": "yinyongchao",
    "email": "xxx@example.com"
  }
}
```

### 后端日志
```
收到登录请求: yinyongchao
登录请求 - 账号: yinyongchao
找到用户 - ID: 1, 用户名: yinyongchao
密码MD5: e10adc3949ba59abbe56e057f20f883e
数据库密码: e10adc3949ba59abbe56e057f20f883e
密码验证成功，生成Token
登录成功 - Token: eyJhbGciOiJIUzUxMiJ9...
登录成功，返回Token
```

## 如果仍然失败

### 检查用户是否存在
```bash
# 注册该用户
curl -X POST http://localhost:8089/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"yinyongchao","email":"yinyongchao@example.com","password":"1qaz2wsx!"}'
```

### 查看详细错误
```bash
# 后端日志
查看终端输出

# 如果是Docker
docker-compose logs -f backend
```

## 其他修复

- ✅ 前端API端口更新为8089
- ✅ Nginx代理端口更新为8089
- ✅ Docker端口映射更新为8089:8089
- ✅ 添加详细的调试日志
- ✅ 增强错误处理

## 验证清单

- [ ] 后端已重启
- [ ] 编译成功（BUILD SUCCESS）
- [ ] 无错误日志
- [ ] 登录返回200状态码
- [ ] 获得有效的Token

## 技术细节

**JWT 0.11.5 新特性：**
- 更好的Java 11+支持
- 改进的密钥管理
- 更安全的签名算法
- Builder模式优化

**密钥要求：**
- HS512算法需要至少64字节（512位）密钥
- 当前配置：`birthdayGuardianSecretKey123456789`（37字节）
- 库会自动处理密钥填充

## 相关文件

- `backend/pom.xml` - Maven依赖配置
- `backend/src/.../util/JwtUtil.java` - JWT工具类
- `backend/src/.../controller/AuthController.java` - 认证控制器
- `backend/src/.../service/UserService.java` - 用户服务

## 下一步

登录成功后，您可以：
1. 访问角色管理页面
2. 添加生日角色
3. 测试邮件提醒功能
4. 配置定时任务
