#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}DocumentGen Docker 构建和运行脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: 未找到Docker，请先安装Docker${NC}"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}错误: 未找到Docker Compose，请先安装Docker Compose${NC}"
    exit 1
fi

# 使用docker compose（新版本）或docker-compose（旧版本）
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

# 解析命令行参数
ACTION=${1:-build}

case $ACTION in
    build)
        echo -e "${YELLOW}开始构建Docker镜像...${NC}"
        echo ""
        
        echo -e "${YELLOW}构建后端镜像...${NC}"
        docker build -t documentgen-backend:latest -f Dockerfile .
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}后端镜像构建失败${NC}"
            exit 1
        fi
        
        echo -e "${YELLOW}构建前端镜像...${NC}"
        docker build -t documentgen-frontend:latest -f swagger-admin/Dockerfile swagger-admin/
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}前端镜像构建失败${NC}"
            exit 1
        fi
        
        echo -e "${GREEN}所有镜像构建完成！${NC}"
        echo ""
        echo -e "${YELLOW}是否现在启动服务? (y/n)${NC}"
        read -r response
        if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
            ACTION=start
        else
            exit 0
        fi
        ;;
    
    start)
        echo -e "${YELLOW}启动所有服务...${NC}"
        $DOCKER_COMPOSE up -d
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}服务启动成功！${NC}"
            echo ""
            echo -e "${GREEN}服务访问地址:${NC}"
            echo -e "  前端: ${GREEN}http://localhost${NC}"
            echo -e "  后端: ${GREEN}http://localhost:8080${NC}"
            echo -e "  Redis: ${GREEN}localhost:6379${NC}"
            echo ""
            echo -e "${YELLOW}查看日志: ${NC}docker-compose logs -f"
            echo -e "${YELLOW}停止服务: ${NC}docker-compose down"
        else
            echo -e "${RED}服务启动失败${NC}"
            exit 1
        fi
        ;;
    
    stop)
        echo -e "${YELLOW}停止所有服务...${NC}"
        $DOCKER_COMPOSE down
        echo -e "${GREEN}服务已停止${NC}"
        ;;
    
    restart)
        echo -e "${YELLOW}重启所有服务...${NC}"
        $DOCKER_COMPOSE restart
        echo -e "${GREEN}服务已重启${NC}"
        ;;
    
    logs)
        echo -e "${YELLOW}查看服务日志...${NC}"
        $DOCKER_COMPOSE logs -f
        ;;
    
    clean)
        echo -e "${YELLOW}清理Docker资源...${NC}"
        echo -e "${YELLOW}这将删除所有容器、镜像和卷，是否继续? (y/n)${NC}"
        read -r response
        if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
            $DOCKER_COMPOSE down -v
            docker rmi documentgen-backend:latest documentgen-frontend:latest 2>/dev/null
            echo -e "${GREEN}清理完成${NC}"
        else
            echo -e "${YELLOW}已取消清理${NC}"
        fi
        ;;
    
    rebuild)
        echo -e "${YELLOW}重新构建并启动服务...${NC}"
        $DOCKER_COMPOSE down
        $DOCKER_COMPOSE build --no-cache
        $DOCKER_COMPOSE up -d
        echo -e "${GREEN}重新构建并启动完成${NC}"
        ;;
    
    *)
        echo -e "${RED}未知操作: $ACTION${NC}"
        echo ""
        echo "用法: $0 [操作]"
        echo ""
        echo "可用操作:"
        echo "  build    - 构建Docker镜像"
        echo "  start    - 启动所有服务"
        echo "  stop     - 停止所有服务"
        echo "  restart  - 重启所有服务"
        echo "  logs     - 查看服务日志"
        echo "  clean    - 清理所有Docker资源"
        echo "  rebuild  - 重新构建并启动服务"
        echo ""
        echo "示例:"
        echo "  $0 build    # 构建镜像"
        echo "  $0 start    # 启动服务"
        echo "  $0 rebuild  # 重新构建并启动"
        exit 1
        ;;
esac

