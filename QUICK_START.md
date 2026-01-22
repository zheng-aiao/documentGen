# 🚀 快速开始指南

## 一键部署（推荐）

### Windows
```cmd
build-and-run.bat build
```

**注意**：如果使用PowerShell脚本遇到执行策略错误，请使用批处理脚本 `build-and-run.bat`，或运行：
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Linux/Mac
```bash
chmod +x build-and-run.sh
./build-and-run.sh build
```

## 📋 使用的Docker镜像（均在Docker Hub上）

- ✅ `maven:3.9-eclipse-temurin-17` - Maven构建工具
- ✅ `eclipse-temurin:17-jre-alpine` - Java 17运行环境
- ✅ `node:20-alpine` - Node.js构建环境
- ✅ `nginx:alpine` - Nginx Web服务器
- ✅ `redis:7-alpine` - Redis缓存服务

## 🌐 访问地址

启动成功后访问：
- **前端**: http://localhost
- **后端**: http://localhost:8080
- **Redis**: localhost:6379

## 📝 常用命令

### Windows
```powershell
.\build-and-run.ps1 build      # 构建
.\build-and-run.ps1 start      # 启动
.\build-and-run.ps1 stop       # 停止
.\build-and-run.ps1 logs       # 日志
.\build-and-run.ps1 clean      # 清理
```

### Linux/Mac
```bash
./build-and-run.sh build        # 构建
./build-and-run.sh start        # 启动
./build-and-run.sh stop         # 停止
./build-and-run.sh logs         # 日志
./build-and-run.sh clean        # 清理
```

## 🔍 验证镜像可用性

### Windows
```powershell
.\verify-docker-images.ps1
```

### Linux/Mac
```bash
chmod +x verify-docker-images.sh
./verify-docker-images.sh
```

## 📚 详细文档

查看 [DOCKER_README.md](./DOCKER_README.md) 获取完整文档。

