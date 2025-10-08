# 第一阶段：构建前端
FROM node:20-alpine3.18 AS frontend-builder

WORKDIR /frontend

# 复制前端代码
COPY frontend/package*.json ./
RUN npm install

COPY frontend .
RUN npm run build

# 第二阶段：构建后端
FROM maven:3.8.5-openjdk-17 AS backend-builder

WORKDIR /app

# 复制pom.xml并下载依赖
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY backend/src ./src

# 从前端构建阶段复制静态资源到后端的static目录
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static

# 构建后端
RUN mvn clean package -DskipTests

# 第三阶段：运行
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制构建的jar文件
COPY --from=backend-builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8089

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]