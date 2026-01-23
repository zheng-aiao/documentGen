# DocumentGen Docker 构建和运行脚本 (Windows PowerShell)

param(
    [Parameter(Position=0)]
    [string]$Action = "build",
    [Parameter(Position=1)]
    [string]$Service = ""
)

# 颜色输出函数
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

# 脚本目录
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

Write-ColorOutput Green "========================================"
Write-ColorOutput Green "DocumentGen Docker 构建和运行脚本"
Write-ColorOutput Green "========================================"
Write-Output ""

# 检查Docker是否安装
try {
    $null = docker --version 2>&1
} catch {
    Write-ColorOutput Red "错误: 未找到Docker，请先安装Docker Desktop"
    exit 1
}

# 检查Docker Compose是否安装
$dockerComposeCmd = $null
try {
    $null = docker compose version 2>&1
    $dockerComposeCmd = "docker compose"
} catch {
    try {
        $null = docker-compose --version 2>&1
        $dockerComposeCmd = "docker-compose"
    } catch {
        Write-ColorOutput Red "错误: 未找到Docker Compose，请先安装Docker Compose"
        exit 1
    }
}

# 执行操作
switch ($Action.ToLower()) {
    "build" {
        Write-ColorOutput Yellow "开始构建Docker镜像..."
        Write-Output ""
        
        Write-ColorOutput Yellow "构建后端镜像..."
        docker build -t documentgen-backend:latest -f Dockerfile .
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "后端镜像构建失败"
            exit 1
        }
        
        Write-ColorOutput Yellow "构建前端镜像..."
        docker build -t documentgen-frontend:latest -f swagger-admin/Dockerfile swagger-admin/
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "前端镜像构建失败"
            exit 1
        }
        
        Write-ColorOutput Green "所有镜像构建完成！"
        Write-Output ""
        $response = Read-Host "是否现在启动服务? (y/n)"
        if ($response -match "^[yY]") {
            $Action = "start"
        } else {
            exit 0
        }
    }
    
    "start-backend" {
        Write-ColorOutput Yellow "启动后端服务..."
        Invoke-Expression "$dockerComposeCmd up -d backend"
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "后端服务启动失败"
            exit 1
        }
        Write-ColorOutput Green "后端服务已启动"
    }
    
    "start-frontend" {
        Write-ColorOutput Yellow "启动前端服务..."
        Invoke-Expression "$dockerComposeCmd up -d frontend"
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "前端服务启动失败"
            exit 1
        }
        Write-ColorOutput Green "前端服务已启动"
    }
    
    "build-backend" {
        Write-ColorOutput Yellow "开始构建后端镜像..."
        docker build -t documentgen-backend:latest -f Dockerfile .
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "后端镜像构建失败"
            exit 1
        }
        
        Write-ColorOutput Green "后端镜像构建完成！"
        Write-Output ""
        $response = Read-Host "是否现在重启后端服务? (y/n)"
        if ($response -match "^[yY]") {
            Invoke-Expression "$dockerComposeCmd up -d --build backend"
            Write-ColorOutput Green "后端服务已重启"
        }
        exit 0
    }
    
    "build-frontend" {
        Write-ColorOutput Yellow "开始构建前端镜像..."
        docker build -t documentgen-frontend:latest -f swagger-admin/Dockerfile swagger-admin/
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "前端镜像构建失败"
            exit 1
        }
        
        Write-ColorOutput Green "前端镜像构建完成！"
        Write-Output ""
        $response = Read-Host "是否现在重启前端服务? (y/n)"
        if ($response -match "^[yY]") {
            Invoke-Expression "$dockerComposeCmd up -d --build frontend"
            Write-ColorOutput Green "前端服务已重启"
        }
        exit 0
    }
    
    "start" {
        Write-ColorOutput Yellow "启动所有服务..."
        Invoke-Expression "$dockerComposeCmd up -d"
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput Green "服务启动成功！"
            Write-Output ""
            Write-ColorOutput Green "服务访问地址:"
            Write-Output "  前端: http://localhost"
            Write-Output "  后端: http://localhost:9001"
            Write-Output "  Redis: localhost:6379"
            Write-Output ""
            Write-ColorOutput Yellow "查看日志: $dockerComposeCmd logs -f"
            Write-ColorOutput Yellow "停止服务: $dockerComposeCmd down"
        } else {
            Write-ColorOutput Red "服务启动失败"
            exit 1
        }
    }
    
    "stop" {
        Write-ColorOutput Yellow "停止所有服务..."
        Invoke-Expression "$dockerComposeCmd down"
        Write-ColorOutput Green "服务已停止"
    }
    
    "stop-backend" {
        Write-ColorOutput Yellow "停止后端服务..."
        Invoke-Expression "$dockerComposeCmd stop backend"
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "后端服务停止失败"
            exit 1
        }
        Write-ColorOutput Green "后端服务已停止"
    }
    
    "stop-frontend" {
        Write-ColorOutput Yellow "停止前端服务..."
        Invoke-Expression "$dockerComposeCmd stop frontend"
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "前端服务停止失败"
            exit 1
        }
        Write-ColorOutput Green "前端服务已停止"
    }
    
    "restart" {
        Write-ColorOutput Yellow "重启所有服务..."
        Invoke-Expression "$dockerComposeCmd restart"
        Write-ColorOutput Green "服务已重启"
    }
    
    "restart-backend" {
        Write-ColorOutput Yellow "重启后端服务..."
        Invoke-Expression "$dockerComposeCmd restart backend"
        Write-ColorOutput Green "后端服务已重启"
    }
    
    "restart-frontend" {
        Write-ColorOutput Yellow "重启前端服务..."
        Invoke-Expression "$dockerComposeCmd restart frontend"
        Write-ColorOutput Green "前端服务已重启"
    }
    
    "logs" {
        if ($Service) {
            Write-ColorOutput Yellow "查看 $Service 服务日志..."
            Invoke-Expression "$dockerComposeCmd logs -f $Service"
        } else {
            Write-ColorOutput Yellow "查看所有服务日志..."
            Invoke-Expression "$dockerComposeCmd logs -f"
        }
    }
    
    "clean" {
        Write-ColorOutput Yellow "清理Docker资源..."
        Write-ColorOutput Yellow "这将删除所有容器、镜像和卷，是否继续? (y/n)"
        $response = Read-Host
        if ($response -match "^[yY]") {
            Invoke-Expression "$dockerComposeCmd down -v"
            docker rmi documentgen-backend:latest documentgen-frontend:latest 2>$null
            Write-ColorOutput Green "清理完成"
        } else {
            Write-ColorOutput Yellow "已取消清理"
        }
    }
    
    "rebuild" {
        Write-ColorOutput Yellow "重新构建并启动服务..."
        Invoke-Expression "$dockerComposeCmd down"
        Invoke-Expression "$dockerComposeCmd build --no-cache"
        Invoke-Expression "$dockerComposeCmd up -d"
        Write-ColorOutput Green "重新构建并启动完成"
    }
    
    default {
        Write-ColorOutput Red "未知操作: $Action"
        Write-Output ""
        Write-Output "用法: .\build-and-run.ps1 [操作] [服务名]"
        Write-Output ""
        Write-Output "可用操作:"
        Write-Output "  build              - 构建所有Docker镜像"
        Write-Output "  build-backend      - 只构建后端镜像"
        Write-Output "  build-frontend     - 只构建前端镜像"
        Write-Output "  start              - 启动所有服务"
        Write-Output "  start-backend      - 只启动后端服务"
        Write-Output "  start-frontend     - 只启动前端服务"
        Write-Output "  stop               - 停止所有服务"
        Write-Output "  stop-backend       - 只停止后端服务"
        Write-Output "  stop-frontend      - 只停止前端服务"
        Write-Output "  restart            - 重启所有服务"
        Write-Output "  restart-backend    - 只重启后端服务"
        Write-Output "  restart-frontend   - 只重启前端服务"
        Write-Output "  logs [服务名]      - 查看服务日志（不指定服务名则查看所有）"
        Write-Output "  clean              - 清理所有Docker资源"
        Write-Output "  rebuild            - 重新构建并启动所有服务"
        Write-Output ""
        Write-Output "示例:"
        Write-Output "  .\build-and-run.ps1 build              # 构建所有镜像"
        Write-Output "  .\build-and-run.ps1 build-backend      # 只构建后端"
        Write-Output "  .\build-and-run.ps1 build-frontend    # 只构建前端"
        Write-Output "  .\build-and-run.ps1 start-backend     # 只启动后端"
        Write-Output "  .\build-and-run.ps1 start-frontend    # 只启动前端"
        Write-Output "  .\build-and-run.ps1 stop-backend      # 只停止后端"
        Write-Output "  .\build-and-run.ps1 stop-frontend     # 只停止前端"
        Write-Output "  .\build-and-run.ps1 restart-backend   # 只重启后端"
        Write-Output "  .\build-and-run.ps1 logs backend       # 查看后端日志"
        exit 1
    }
}

# 如果build后选择了start，继续执行start
if ($Action -eq "start") {
    Write-ColorOutput Yellow "启动所有服务..."
    Invoke-Expression "$dockerComposeCmd up -d"
    
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput Green "服务启动成功！"
        Write-Output ""
        Write-ColorOutput Green "服务访问地址:"
        Write-Output "  前端: http://localhost"
        Write-Output "  后端: http://localhost:9001"
        Write-Output "  Redis: localhost:6379"
        Write-Output ""
        Write-ColorOutput Yellow "查看日志: $dockerComposeCmd logs -f"
        Write-ColorOutput Yellow "停止服务: $dockerComposeCmd down"
    } else {
        Write-ColorOutput Red "服务启动失败"
        exit 1
    }
}
