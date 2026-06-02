# DayPlan 应用镜像构建
FROM eclipse-temurin:17-jre-alpine

# 环境参数
ENV PORT=9999
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV TZ=Asia/Shanghai

# 暴露端口
EXPOSE $PORT

# 时区配置
RUN apk add --no-cache tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo 'Asia/Shanghai' > /etc/timezone

# 日志目录
VOLUME /tmp /app/logs

# 复制 JAR 包（Spotify docker-maven-plugin 会将 JAR 复制到 target 目录）
ARG JAR_FILE=dayplan-app.jar
ADD ${JAR_FILE} /app.jar
RUN bash -c 'touch /app.jar'

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dlog4j2.formatMsgNoLookups=true -server -jar /app.jar --server.port=${PORT}"]