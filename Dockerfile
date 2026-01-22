# 使用多阶段构建
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 先复制本地JAR文件和pom.xml
COPY lib ./lib
COPY pom.xml .

# 安装本地JAR到Maven本地仓库
RUN mvn install:install-file \
    -Dfile=./lib/aspose-words-15.8.0.jar \
    -DgroupId=com.aspose \
    -DartifactId=aspose-words \
    -Dversion=15.8.0 \
    -Dpackaging=jar && \
    mvn install:install-file \
    -Dfile=./lib/aspose-pdf-11.8.0.jar \
    -DgroupId=com.aspose \
    -DartifactId=aspose-pdf \
    -Dversion=11.8.0 \
    -Dpackaging=jar

# 下载依赖（利用 Docker 层缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

# 安装curl用于健康检查
RUN apk add --no-cache curl

# 创建应用目录和用户
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# 创建文档输出目录
RUN mkdir -p /var/numax/genew && \
    chown -R appuser:appuser /var/numax

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 更改文件所有者
RUN chown -R appuser:appuser /app

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查（如果actuator不可用，使用根路径）
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1

# 启动应用（激活docker profile）
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]