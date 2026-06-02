# ============================================
# Docker 镜像构建文件
# 用于将 Spring Boot 项目打包成 Docker 容器
# ============================================

# ------------------------------
# 基础镜像说明
# ------------------------------
# eclipse-temurin：开源免费的 JDK 发行版（Oracle JDK 的开源替代）
# 17-jre-alpine：JDK 17 运行时环境，基于 Alpine Linux（轻量级，约 200MB）
# alpine 优点：体积小、启动快、安全性高
FROM eclipse-temurin:17-jre-alpine

# ------------------------------
# 环境变量配置
# ------------------------------
# TZ：设置容器时区为中国上海，确保日志时间准确
ENV TZ=Asia/Shanghai

# RUN：构建时执行的命令
# apk add：Alpine 包管理器安装软件
# --no-cache：不缓存下载文件，减小镜像体积
# ln -sf：创建软链接，将上海时区链接到容器时区文件
# echo：将时区名称写入配置文件
RUN apk add --no-cache tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo 'Asia/Shanghai' > /etc/timezone

# LANG/LC_ALL：设置字符集为 UTF-8，避免中文乱码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

# ------------------------------
# 工作目录设置
# ------------------------------
# WORKDIR：设置后续命令的工作目录，相当于 cd /app
# 容器启动后默认就在这个目录
WORKDIR /app

# ------------------------------
# 复制 JAR 文件
# ------------------------------
# ARG：定义构建参数，在 docker build 时传入
# ${PROJECT_NAME}：由 start.sh 传入的项目名，用于定位 target 目录下的 jar 文件
# app.jar：复制到容器内的文件名（固定名称）
ARG PROJECT_NAME
COPY target/${PROJECT_NAME}.jar app.jar

# ------------------------------
# 端口声明
# ------------------------------
# EXPOSE：声明容器监听端口，仅作为文档说明
# 实际端口映射在 docker-compose.yml 的 ports 中定义
# ${PORT}：从 .env 读取的服务端口
EXPOSE ${PORT}

# ------------------------------
# 启动命令
# ------------------------------
# ENTRYPOINT：容器启动时执行的命令（不可被 docker-compose run 覆盖）
# sh -c：执行后面的字符串作为 shell 命令
# java $JAVA_OPTS：使用 .env 中定义的 JVM 参数启动
# -jar app.jar：运行 jar 包
# --server.port=${PORT}：Spring Boot 监听端口，与 EXPOSE 一致
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]