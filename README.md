# 生日守护者系统

一个完整的生日提醒管理系统，帮助您记住重要人物的生日并提前发送邮件提醒。

## 功能特性

- ✅ 用户注册和登录（支持邮箱和用户名登录）
- ✅ 生日角色管理（每个用户最多添加20个角色）
- ✅ 支持阳历和阴历生日
- ✅ 自动阴阳历转换
- ✅ 自定义提前提醒天数
- ✅ 邮件提醒功能
- ✅ 每年自动提醒
- ✅ Docker一键部署

## 技术栈

### 后端
- Java 17
- Spring Boot 2.7.14
- MyBatis-Plus 3.5.3.1
- MySQL 8.0
- JWT
- Spring Mail

### 前端
- Vue 3
- Vite
- Element Plus
- Axios
- Vue Router 4

### 部署
- Docker
- Docker Compose
- Nginx

## 快速开始（推荐Docker部署）

### 方式一：Docker一键部署（推荐）

#### 1. 环境准备

确保已安装：
- Docker
- Docker Compose

#### 2. 配置邮箱（重要）

```bash
# 复制配置文件模板
cp .env.example .env

# 编辑.env文件，修改邮箱配置
# SPRING_MAIL_USERNAME=your-email@gmail.com
# SPRING_MAIL_PASSWORD=your-app-password
```

**Gmail邮箱配置说明：**
1. 登录Gmail账户
2. 开启两步验证
3. 生成应用专用密码
4. 将生成的密码填入 `SPRING_MAIL_PASSWORD`

#### 3. 启动服务

```bash
# 一键启动所有服务
./docker-start.sh

# 或使用docker-compose命令
docker-compose up -d
```

#### 4. 访问应用

- **前端地址**: http://localhost
- **后端API**: http://localhost:8080
- **MySQL**: localhost:3306

#### 5. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

#### 6. 停止服务

```bash
# 停止所有服务
./docker-stop.sh

# 或使用docker-compose命令
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

---

### 方式二：本地开发部署

#### 1. 数据库准备

确保MySQL正在运行。执行数据库脚本：

```bash
mysql -uroot -proot < backend/src/main/resources/schema.sql
```

#### 2. 后端启动

```bash
cd backend

# 修改 src/main/resources/application.yml 配置邮箱信息

# 编译并启动
mvn clean package -DskipTests
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

#### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 http://localhost:5173 启动

---

## 项目结构

```
BirthdayGuardian-1/
├── backend/                      # Spring Boot后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/birthday/guardian/
│   │   │   │       ├── controller/    # 控制器层
│   │   │   │       ├── service/       # 业务层
│   │   │   │       ├── mapper/        # 数据访问层
│   │   │   │       ├── entity/        # 实体类
│   │   │   │       ├── dto/           # 数据传输对象
│   │   │   │       ├── util/          # 工具类
│   │   │   │       ├── config/        # 配置类
│   │   │   │       ├── common/        # 通用类
│   │   │   │       └── interceptor/   # 拦截器
│   │   │   └── resources/
│   │   │       ├── application.yml    # 配置文件
│   │   │       └── schema.sql         # 数据库脚本
│   │   └── test/
│   ├── Dockerfile                # 后端Docker配置
│   └── pom.xml
├── frontend/                     # Vue3前端
│   ├── src/
│   │   ├── api/                 # API接口
│   │   ├── views/               # 页面组件
│   │   ├── router/              # 路由配置
│   │   ├── utils/               # 工具函数
│   │   ├── App.vue
│   │   └── main.js
│   ├── nginx.conf               # Nginx配置
│   ├── Dockerfile               # 前端Docker配置
│   └── package.json
├── docker-compose.yml           # Docker编排配置
├── .env.example                 # 环境变量模板
├── docker-start.sh              # Docker启动脚本
├── docker-stop.sh               # Docker停止脚本
├── start.sh                     # 本地启动脚本
└── README.md
```

## Docker架构

系统使用Docker Compose编排三个服务：

1. **MySQL** (birthday-guardian-mysql)
   - 数据持久化
   - 健康检查
   - 自动初始化数据库

2. **Backend** (birthday-guardian-backend)
   - Java Spring Boot应用
   - 多阶段构建优化镜像大小
   - 等待MySQL健康后启动

3. **Frontend** (birthday-guardian-frontend)
   - Vue3 + Nginx
   - 静态资源优化
   - API反向代理到后端

服务间通过 `birthday-network` 网络互联。

## API接口文档

### 认证接口

#### 注册
- **POST** `/api/auth/register`
- Body:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "123456"
}
```

#### 登录
- **POST** `/api/auth/login`
- Body:
```json
{
  "account": "testuser",
  "password": "123456"
}
```

### 角色管理接口（需要认证）

#### 获取角色列表
- **GET** `/api/roles`
- Headers: `Authorization: Bearer {token}`

#### 添加角色
- **POST** `/api/roles`
- Headers: `Authorization: Bearer {token}`
- Body:
```json
{
  "roleType": "父亲",
  "roleName": "爸爸",
  "birthDate": "1970-01-01",
  "calendarType": 1,
  "remindDays": 3,
  "remark": "记得准备礼物"
}
```

#### 更新角色
- **PUT** `/api/roles/{id}`
- Headers: `Authorization: Bearer {token}`

#### 删除角色
- **DELETE** `/api/roles/{id}`
- Headers: `Authorization: Bearer {token}`

## 数据库表结构

### 用户表（user）
- id: 主键
- username: 用户名（唯一）
- email: 邮箱（唯一）
- password: 密码（MD5加密）
- create_time: 创建时间
- update_time: 更新时间

### 生日角色表（birthday_role）
- id: 主键
- user_id: 用户ID
- role_type: 角色类型
- role_name: 角色名称
- birth_date: 出生日期
- calendar_type: 日历类型（1-阳历，2-阴历）
- lunar_birth_date: 阴历生日
- remind_days: 提前提醒天数
- remark: 备注
- create_time: 创建时间
- update_time: 更新时间

## 定时任务

系统每天上午9点执行生日检查任务，对于即将到来的生日会发送邮件提醒。

可以通过环境变量 `SCHEDULE_CHECK_TIME` 修改执行时间（Cron表达式）。

## 环境变量配置

在 `.env` 文件中可配置以下参数：

```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/birthday_guardian
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root

# 邮件配置
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# JWT配置
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# 定时任务配置
SCHEDULE_CHECK_TIME=0 0 9 * * ?

# 时区
TZ=Asia/Shanghai
```

## 常见问题

### 1. 邮件发送失败

**问题**: 系统无法发送提醒邮件

**解决方案**:
- 检查 `.env` 文件中的邮箱配置是否正确
- 确认使用的是应用专用密码，而非账户密码
- 查看后端日志: `docker-compose logs -f backend`

### 2. 数据库连接失败

**问题**: 后端无法连接到MySQL

**解决方案**:
- 等待MySQL完全启动（通常需要10-20秒）
- 检查MySQL健康状态: `docker-compose ps`
- 查看MySQL日志: `docker-compose logs -f mysql`

### 3. 前端无法访问后端API

**问题**: 前端请求后端接口失败

**解决方案**:
- 检查nginx配置中的代理设置
- 确认后端服务正常运行: `docker-compose logs -f backend`
- 检查网络连接: `docker network inspect birthdayguardian-1_birthday-network`

### 4. 端口被占用

**问题**: 启动失败，提示端口已被占用

**解决方案**:
- 修改 `docker-compose.yml` 中的端口映射
- 或停止占用端口的其他服务

## 维护命令

```bash
# 重新构建镜像
docker-compose build --no-cache

# 查看运行状态
docker-compose ps

# 进入容器
docker-compose exec backend bash
docker-compose exec mysql bash

# 查看资源占用
docker stats

# 清理未使用的镜像和容器
docker system prune -a
```

## 备份与恢复

### 备份数据库

```bash
docker-compose exec mysql mysqldump -uroot -proot birthday_guardian > backup.sql
```

### 恢复数据库

```bash
docker-compose exec -T mysql mysql -uroot -proot birthday_guardian < backup.sql
```

## 性能优化建议

1. **生产环境部署**:
   - 修改MySQL密码
   - 使用更强的JWT密钥
   - 配置反向代理（如Nginx）
   - 启用HTTPS

2. **数据库优化**:
   - 定期备份数据
   - 添加索引优化查询
   - 监控慢查询

3. **应用优化**:
   - 使用Redis缓存
   - 配置日志级别
   - 限制API请求频率

## 技术支持

遇到问题？

1. 查看日志: `docker-compose logs -f`
2. 检查配置: 确认 `.env` 文件配置正确
3. 重启服务: `docker-compose restart`

## 注意事项

1. 每个用户最多可以添加20个角色
2. 密码使用MD5加密存储
3. JWT Token有效期为24小时
4. 邮件提醒需要正确配置邮箱服务
5. 阴历转换使用自实现的算法库（1900-2100年）
6. Docker部署时数据存储在数据卷中，删除容器不会丢失数据

## License

MIT
