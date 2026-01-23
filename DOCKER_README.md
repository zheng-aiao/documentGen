# DocumentGen Docker 部署指南

本项目已配置完整的Docker部署方案，支持一键构建和运行。

## 📋 前置要求

- Docker Desktop (Windows/Mac) 或 Docker Engine (Linux)
- Docker Compose (通常随Docker一起安装)

## 🚀 快速开始

### Windows 系统

**方式一：使用批处理脚本（推荐，无需修改执行策略）**
```cmd
# 构建并启动所有服务
build-and-run.bat build

# 或者分步执行
build-and-run.bat build    # 构建镜像
build-and-run.bat start    # 启动服务
```

**方式二：使用PowerShell脚本（需要修改执行策略）**
```powershell
# 如果遇到执行策略错误，先运行以下命令（管理员权限）：
# Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 然后执行：
.\build-and-run.ps1 build
```

### Linux/Mac 系统

```bash
# 添加执行权限（首次运行）
chmod +x build-and-run.sh

# 构建并启动所有服务
./build-and-run.sh build

# 或者分步执行
./build-and-run.sh build    # 构建镜像
./build-and-run.sh start    # 启动服务
```

## 📦 项目结构

```
documentGen/
├── Dockerfile                    # 后端Dockerfile
├── docker-compose.yml            # 开发环境配置
├── docker-compose.prod.yml       # 生产环境配置
├── .dockerignore                 # Docker忽略文件
├── build-and-run.sh             # Linux/Mac构建脚本
├── build-and-run.ps1            # Windows构建脚本
├── swagger-admin/
│   ├── Dockerfile               # 前端Dockerfile
│   └── nginx.conf               # Nginx配置
└── src/main/resources/
    └── application-docker.properties  # Docker环境配置
```

## 🐳 Docker 镜像说明

### 后端镜像 (documentgen-backend)
- **基础镜像**: `eclipse-temurin:17-jre-alpine`
- **构建镜像**: `maven:3.9.4`
- **端口**: 9001
- **特点**: 
  - 多阶段构建，减小镜像体积
  - 非root用户运行，提高安全性
  - 包含健康检查

### 前端镜像 (documentgen-frontend)
- **构建镜像**: `node:20-alpine`
- **运行镜像**: `nginx:alpine`
- **端口**: 80
- **特点**:
  - 多阶段构建，只包含构建产物
  - Nginx反向代理配置
  - 支持前端路由和API代理

### Redis镜像
- **镜像**: `redis:7-alpine`
- **端口**: 6379
- **密码**: Nucleus! (可在docker-compose.yml中修改)

## 🔧 常用命令

### 使用脚本（推荐）

#### Windows
```cmd
build-and-run.bat build      # 构建镜像
build-and-run.bat start       # 启动服务
build-and-run.bat stop        # 停止服务
build-and-run.bat restart     # 重启服务
build-and-run.bat logs        # 查看日志
build-and-run.bat clean       # 清理所有资源
build-and-run.bat rebuild     # 重新构建并启动
```

**或者使用PowerShell（需要先修改执行策略）**
```powershell
.\build-and-run.ps1 build      # 构建镜像
.\build-and-run.ps1 start       # 启动服务
# ... 其他命令相同
```

#### Linux/Mac
```bash
./build-and-run.sh build        # 构建镜像
./build-and-run.sh start        # 启动服务
./build-and-run.sh stop         # 停止服务
./build-and-run.sh restart      # 重启服务
./build-and-run.sh logs         # 查看日志
./build-and-run.sh clean        # 清理所有资源
./build-and-run.sh rebuild      # 重新构建并启动
```

### 使用 Docker Compose 直接命令

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f redis

# 停止服务
docker-compose down

# 停止并删除卷
docker-compose down -v

# 重新构建并启动
docker-compose up -d --build
```

## 🌐 访问地址

启动成功后，可以通过以下地址访问：

- **前端**: http://localhost
- **后端API**: http://localhost:9001
- **Redis**: localhost:6379

## ⚙️ 环境配置

### 开发环境

使用 `docker-compose.yml`，配置已预设好。

### 生产环境

使用 `docker-compose.prod.yml`，支持环境变量配置：

```bash
# 创建 .env 文件
cat > .env << EOF
REDIS_PASSWORD=your_secure_password
GENEW_SERVER_URL=https://your-server.com/swagger/swagger-resources
EOF

# 使用生产配置启动
docker-compose -f docker-compose.prod.yml up -d
```

### 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `SPRING_DATA_REDIS_HOST` | Redis主机地址 | redis |
| `SPRING_DATA_REDIS_PORT` | Redis端口 | 6379 |
| `SPRING_DATA_REDIS_PASSWORD` | Redis密码 | Nucleus! |
| `GENEW_SERVER_URL` | Genew服务器地址 | https://newdev.rdapp.com:53839/swagger/swagger-resources |
| `GENEW_DOCUMENT_OUTPUT_PATH` | 文档输出路径 | /var/numax/genew |

## 📝 数据持久化

项目使用Docker卷来持久化数据：

- `redis-data`: Redis数据持久化
- `document-output`: 生成的文档输出目录

数据卷位置（Linux）:
```bash
docker volume inspect documentgen_redis-data
docker volume inspect documentgen_document-output
```

## 🚚 迁移

### 导出所有相关镜像

在源服务器上，导出所有镜像（包括基础镜像，适用于离线环境）：

```bash
# 导出所有镜像到单个文件
docker save \
  documentgen-backend:latest \
  documentgen-frontend:latest \
  eclipse-temurin:17-jre-alpine \
  maven:3.9-eclipse-temurin-17 \
  nginx:alpine \
  node:20-alpine \
  redis:7-alpine \
  -o documentgen-all-images.tar
```

### 导出卷数据（可选，如果需要保留数据）

```bash
# 停止服务（确保数据一致性）
docker-compose down

# 导出 Redis 数据
docker run --rm -v documentgen_redis-data:/data -v $(pwd):/backup alpine \
  tar czf /backup/redis-data.tar.gz -C /data .

# 导出文档输出数据
docker run --rm -v documentgen_document-output:/data -v $(pwd):/backup alpine \
  tar czf /backup/document-output.tar.gz -C /data .
```

### 在新服务器上导入

```bash
# 1. 导入镜像
docker load -i documentgen-all-images.tar

# 2. 如果导出了卷数据，先启动服务创建卷，然后恢复数据
docker-compose up -d
docker-compose down

# 恢复 Redis 数据
docker run --rm -v documentgen_redis-data:/data -v $(pwd):/backup alpine \
  sh -c "cd /data && tar xzf /backup/redis-data.tar.gz"

# 恢复文档输出数据
docker run --rm -v documentgen_document-output:/data -v $(pwd):/backup alpine \
  sh -c "cd /data && tar xzf /backup/document-output.tar.gz"

# 3. 启动服务
# Linux/Mac
./build-and-run.sh start

# Windows
build-and-run.bat start

# 或使用 docker-compose
docker-compose up -d
```

### 检查迁移结果

```bash
# 检查镜像是否导入成功
docker images | grep documentgen

# 检查容器是否正常运行
docker ps

# 或使用 docker-compose
docker-compose ps
```

### 注意事项

1. **如果新服务器有网络**：可以只导出自定义镜像（`documentgen-backend:latest` 和 `documentgen-frontend:latest`），基础镜像会自动从 Docker Hub 拉取
2. **如果新服务器无网络**：必须导出所有镜像，包括基础镜像
3. **卷数据迁移**：如果不需要保留历史数据，可以跳过卷数据导出步骤，直接启动新服务
4. **文件传输**：使用 `scp`、`rsync` 或其他方式将 tar 文件传输到新服务器

## 🔍 故障排查

### 1. 端口被占用

如果9001或80端口被占用，可以修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "9002:9001"  # 改为9002
```

### 2. 后端无法连接Redis

检查Redis服务是否正常：
```bash
docker-compose logs redis
docker-compose exec redis redis-cli ping
```

### 3. 前端无法访问后端

检查网络连接和Nginx配置：
```bash
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf
docker-compose logs frontend
```

### 4. 查看容器状态

```bash
docker-compose ps
docker ps
```

### 5. 进入容器调试

```bash
# 进入后端容器
docker-compose exec backend sh

# 进入前端容器
docker-compose exec frontend sh

# 进入Redis容器
docker-compose exec redis sh
```

## 🛠️ 开发模式

如果需要开发模式（代码热更新），可以：

1. 本地运行后端和前端
2. 只使用Docker运行Redis：
   ```bash
   docker-compose up redis
   ```

## 📚 相关文档

- [Docker官方文档](https://docs.docker.com/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [Spring Boot Docker指南](https://spring.io/guides/gs/spring-boot-docker/)

## ⚠️ 注意事项

1. **首次构建可能需要较长时间**，因为需要下载基础镜像和依赖
2. **确保Docker有足够的资源**（建议至少2GB内存）
3. **生产环境请修改默认密码**（Redis密码等）
4. **文档输出目录**需要确保有写入权限
5. **网络配置**：确保容器间可以正常通信

## 🐛 问题反馈

如遇到问题，请检查：
1. Docker和Docker Compose版本是否最新
2. 系统资源是否充足
3. 端口是否被占用
4. 查看容器日志获取详细错误信息

