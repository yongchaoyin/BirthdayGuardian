# 使用官方的 OpenJDK 17 镜像作为基础镜像
FROM eclipse-temurin:17-jdk

# 复制jar包至镜像内
# 复制的目录需放置在与 Dockerfile 文件同级的目录下
COPY target/guardian-1.0.0.jar /guardian-1.0.0.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 容器启动执行命令
ENTRYPOINT ["java", "-jar", "/guardian-1.0.0.jar", "--spring.profiles.active=prod"]

# 对外暴露的端口号
EXPOSE 8089