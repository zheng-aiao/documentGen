@echo off
REM DocumentGen Docker 构建和运行脚�?(Windows Batch)
REM 设置代码页为GBK以正确显示中文（中文Windows默认编码�?
chcp 936 >nul 2>&1

setlocal enabledelayedexpansion

REM 脚本目录
cd /d "%~dp0"

echo ========================================
echo DocumentGen Docker 构建和运行脚�?
echo ========================================
echo.

REM 检查Docker是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Docker，请先安装Docker Desktop
    exit /b 1
)

REM 检查Docker Compose是否安装
set DOCKER_COMPOSE_CMD=
docker compose version >nul 2>&1
if errorlevel 1 (
    docker-compose --version >nul 2>&1
    if errorlevel 1 (
        echo [错误] 未找到Docker Compose，请先安装Docker Compose
        exit /b 1
    ) else (
        set DOCKER_COMPOSE_CMD=docker-compose
    )
) else (
    set DOCKER_COMPOSE_CMD=docker compose
)

REM 解析命令行参�?
set ACTION=%1
if "%ACTION%"=="" set ACTION=build

if /i "%ACTION%"=="build" goto :build
if /i "%ACTION%"=="start" goto :start
if /i "%ACTION%"=="stop" goto :stop
if /i "%ACTION%"=="restart" goto :restart
if /i "%ACTION%"=="logs" goto :logs
if /i "%ACTION%"=="clean" goto :clean
if /i "%ACTION%"=="rebuild" goto :rebuild
goto :usage

:build
echo [信息] 开始构建Docker镜像...
echo.
echo [信息] 构建后端镜像...
docker build -t documentgen-backend:latest -f Dockerfile .
if errorlevel 1 (
    echo [错误] 后端镜像构建失败
    exit /b 1
)

echo [信息] 构建前端镜像...
docker build -t documentgen-frontend:latest -f swagger-admin/Dockerfile swagger-admin/
if errorlevel 1 (
    echo [错误] 前端镜像构建失败
    exit /b 1
)

echo [成功] 所有镜像构建完成！
echo.
set /p response="是否现在启动服务? (y/n): "
if /i "!response!"=="y" (
    set ACTION=start
    goto :start
) else (
    exit /b 0
)

:start
echo [信息] 启动所有服�?..
call %DOCKER_COMPOSE_CMD% up -d
if errorlevel 1 (
    echo [错误] 服务启动失败
    exit /b 1
) else (
    echo [成功] 服务启动成功�?
    echo.
    echo [信息] 服务访问地址:
    echo   前端: http://localhost
    echo   后端: http://localhost:8080
    echo   Redis: localhost:6379
    echo.
    echo [提示] 查看日志: %DOCKER_COMPOSE_CMD% logs -f
    echo [提示] 停止服务: %DOCKER_COMPOSE_CMD% down
)
goto :end

:stop
echo [信息] 停止所有服�?..
call %DOCKER_COMPOSE_CMD% down
echo [成功] 服务已停�?
goto :end

:restart
echo [信息] 重启所有服�?..
call %DOCKER_COMPOSE_CMD% restart
echo [成功] 服务已重�?
goto :end

:logs
echo [信息] 查看服务日志...
call %DOCKER_COMPOSE_CMD% logs -f
goto :end

:clean
echo [信息] 清理Docker资源...
echo [警告] 这将删除所有容器、镜像和卷，是否继续? (y/n)
set /p response=
if /i "!response!"=="y" (
    call %DOCKER_COMPOSE_CMD% down -v
    docker rmi documentgen-backend:latest documentgen-frontend:latest 2>nul
    echo [成功] 清理完成
) else (
    echo [信息] 已取消清�?
)
goto :end

:rebuild
echo [信息] 重新构建并启动服�?..
call %DOCKER_COMPOSE_CMD% down
call %DOCKER_COMPOSE_CMD% build --no-cache
call %DOCKER_COMPOSE_CMD% up -d
echo [成功] 重新构建并启动完�?
goto :end

:usage
echo [错误] 未知操作: %ACTION%
echo.
echo 用法: %~nx0 [操作]
echo.
echo 可用操作:
echo   build    - 构建Docker镜像
echo   start    - 启动所有服�?
echo   stop     - 停止所有服�?
echo   restart  - 重启所有服�?
echo   logs     - 查看服务日志
echo   clean    - 清理所有Docker资源
echo   rebuild  - 重新构建并启动服�?
echo.
echo 示例:
echo   %~nx0 build    # 构建镜像
echo   %~nx0 start    # 启动服务
echo   %~nx0 rebuild  # 重新构建并启�?
exit /b 1

:end
endlocal


