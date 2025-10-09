# 构建阶段
FROM maven:3.8.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制所有必要文件并直接构建（跳过预下载依赖步骤）
COPY pom.xml .
COPY src/main/java ./src/main/java
COPY src/main/resources/application.yml ./src/main/resources/application.yml
COPY src/main/resources/schema.sql ./src/main/resources/schema.sql

# 直接构建项目（Maven会自动下载依赖）
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制构建的jar文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8089

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]