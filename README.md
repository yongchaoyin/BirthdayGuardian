# 生日守护者 (Birthday Guardian)

一个基于Spring Boot + Vue3的生日提醒系统，帮助您记住重要人物的生日并及时提醒。

## 功能特性

- 🎂 **生日管理**：添加、编辑、删除生日角色信息
- 📅 **农历支持**：支持农历和公历生日设置
- 📧 **邮件提醒**：自动发送生日提醒邮件
- ⏰ **定时任务**：每日自动检查并发送提醒
- 📊 **统计面板**：直观显示生日统计信息
- 📝 **通知日志**：记录所有提醒发送状态
- 🔐 **用户认证**：支持邮箱验证码和密码登录

## 技术栈

### 后端
- **Spring Boot 3.2.0** - 主框架
- **MyBatis Plus** - ORM框架
- **MySQL** - 数据库
- **Redis** - 缓存和验证码存储
- **Spring Mail** - 邮件发送
- **Spring Security** - 安全框架
- **Spring Task** - 定时任务

### 前端
- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Element Plus** - UI组件库
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Axios** - HTTP客户端

## 项目结构

```
BirthdayGuardian/
├── backend/                    # 后端项目
│   ├── src/main/java/com/birthdayguardian/
│   │   ├── common/            # 通用类
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器
│   │   ├── entity/            # 实体类
│   │   ├── mapper/            # 数据访问层
│   │   ├── scheduler/         # 定时任务
│   │   ├── service/           # 业务逻辑层
│   │   └── BirthdayGuardianApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml    # 配置文件
│   │   └── init.sql          # 数据库初始化脚本
│   └── pom.xml               # Maven配置
├── frontend/                  # 前端项目
│   ├── src/
│   │   ├── api/              # API接口
│   │   ├── components/       # 组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # 状态管理
│   │   ├── style/            # 样式文件
│   │   ├── views/            # 页面组件
│   │   ├── App.vue           # 根组件
│   │   └── main.js           # 入口文件
│   ├── package.json          # 依赖配置
│   ├── vite.config.js        # Vite配置
│   └── index.html            # HTML模板
└── README.md                 # 项目说明
```

## 快速开始

### 环境要求

- Java 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE birthday_guardian CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p birthday_guardian < backend/src/main/resources/init.sql
```

### 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 修改配置文件 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/birthday_guardian
    username: your_username
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
  
  mail:
    host: smtp.qq.com
    username: your_email@qq.com
    password: your_email_password
```

3. 启动应用：
```bash
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端应用将在 `http://localhost:5173` 启动

## 配置说明

### 邮件配置

在 `application.yml` 中配置邮件服务：

```yaml
spring:
  mail:
    host: smtp.qq.com          # SMTP服务器
    port: 587                  # SMTP端口
    username: your_email@qq.com # 发送邮箱
    password: your_auth_code   # 邮箱授权码
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  mail:
    from: your_email@qq.com    # 发件人邮箱
    subject-prefix: "[生日守护者]" # 邮件主题前缀
```

### 定时任务配置

```yaml
app:
  scheduler:
    notify-cron: "0 0 9 * * ?"  # 每天上午9点执行
```

## API接口

### 认证接口
- `POST /api/auth/sendCode` - 发送验证码
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 用户接口
- `GET /api/user/profile` - 获取用户信息
- `POST /api/user/setPassword` - 设置密码

### 角色接口
- `GET /api/role/list` - 获取角色列表
- `POST /api/role/add` - 添加角色
- `PUT /api/role/update` - 更新角色
- `DELETE /api/role/delete/{id}` - 删除角色

### 提醒接口
- `GET /api/reminder/list` - 获取提醒列表
- `POST /api/reminder/add` - 添加提醒
- `DELETE /api/reminder/delete/{id}` - 删除提醒

### 日志接口
- `GET /api/noticeLog/list` - 获取通知日志

## 部署说明

### 后端部署

1. 打包应用：
```bash
./mvnw clean package -DskipTests
```

2. 运行JAR文件：
```bash
java -jar target/birthday-guardian-1.0.0.jar
```

### 前端部署

1. 构建生产版本：
```bash
npm run build
```

2. 将 `dist` 目录部署到Web服务器

### Docker部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: birthday_guardian
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/src/main/resources/init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:6.0
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/birthday_guardian
      SPRING_DATA_REDIS_HOST: redis

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

启动服务：
```bash
docker-compose up -d
```

## 开发指南

### 添加新功能

1. **后端开发**：
   - 在 `entity` 包中创建实体类
   - 在 `mapper` 包中创建数据访问接口
   - 在 `service` 包中实现业务逻辑
   - 在 `controller` 包中创建REST接口

2. **前端开发**：
   - 在 `api` 目录中添加API接口
   - 在 `views` 目录中创建页面组件
   - 在 `router` 中配置路由
   - 在 `stores` 中管理状态

### 代码规范

- 后端遵循阿里巴巴Java开发手册
- 前端遵循Vue.js官方风格指南
- 使用ESLint和Prettier格式化代码

## 常见问题

### Q: 邮件发送失败怎么办？
A: 请检查邮件配置是否正确，确保使用了正确的SMTP服务器和授权码。

### Q: 定时任务不执行怎么办？
A: 确保在主类上添加了 `@EnableScheduling` 注解，并检查cron表达式是否正确。

### Q: 前端页面空白怎么办？
A: 检查后端服务是否正常启动，确保API接口可以正常访问。

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件至：your-email@example.com

---

感谢使用生日守护者！🎉
