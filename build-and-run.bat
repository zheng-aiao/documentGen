@echo off
REM DocumentGen Docker Build and Run Script (Windows Batch)
REM Fix Chinese encoding issue by using UTF-8
chcp 65001 >nul 2>&1

setlocal enabledelayedexpansion

REM Script directory
cd /d "%~dp0"

echo ========================================
echo DocumentGen Docker Build and Run Script
echo ========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker not found, please install Docker Desktop first
    exit /b 1
)

REM Check if Docker Compose is installed
set DOCKER_COMPOSE_CMD=
docker compose version >nul 2>&1
if errorlevel 1 (
    docker-compose --version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Docker Compose not found, please install Docker Compose first
        exit /b 1
    ) else (
        set DOCKER_COMPOSE_CMD=docker-compose
    )
) else (
    set DOCKER_COMPOSE_CMD=docker compose
)

REM Parse command line arguments
set ACTION=%1
set SERVICE=%2
if "%ACTION%"=="" set ACTION=build

if /i "%ACTION%"=="build" goto :build
if /i "%ACTION%"=="build-backend" goto :build_backend
if /i "%ACTION%"=="build-frontend" goto :build_frontend
if /i "%ACTION%"=="start" goto :start
if /i "%ACTION%"=="start-backend" goto :start_backend
if /i "%ACTION%"=="start-frontend" goto :start_frontend
if /i "%ACTION%"=="stop" goto :stop
if /i "%ACTION%"=="stop-backend" goto :stop_backend
if /i "%ACTION%"=="stop-frontend" goto :stop_frontend
if /i "%ACTION%"=="restart" goto :restart
if /i "%ACTION%"=="restart-backend" goto :restart_backend
if /i "%ACTION%"=="restart-frontend" goto :restart_frontend
if /i "%ACTION%"=="logs" goto :logs
if /i "%ACTION%"=="clean" goto :clean
if /i "%ACTION%"=="rebuild" goto :rebuild
goto :usage

:build
echo [INFO] Building all Docker images...
echo.
echo [INFO] Building backend image...
docker build -t documentgen-backend:latest -f Dockerfile .
if errorlevel 1 (
    echo [ERROR] Backend image build failed
    exit /b 1
)

echo [INFO] Building frontend image...
docker build -t documentgen-frontend:latest -f swagger-admin/Dockerfile swagger-admin/
if errorlevel 1 (
    echo [ERROR] Frontend image build failed
    exit /b 1
)

echo [SUCCESS] All images built successfully!
echo.
set /p response="Start services now? (y/n): "
if /i "!response!"=="y" (
    set ACTION=start
    goto :start
) else (
    exit /b 0
)

:build_backend
echo [INFO] Building backend image...
docker build -t documentgen-backend:latest -f Dockerfile .
if errorlevel 1 (
    echo [ERROR] Backend image build failed
    exit /b 1
)
echo [SUCCESS] Backend image built successfully!
echo.
set /p response="Restart backend service now? (y/n): "
if /i "!response!"=="y" (
    call %DOCKER_COMPOSE_CMD% up -d --build backend
    echo [SUCCESS] Backend service restarted
)
goto :end

:build_frontend
echo [INFO] Building frontend image...
docker build -t documentgen-frontend:latest -f swagger-admin/Dockerfile swagger-admin/
if errorlevel 1 (
    echo [ERROR] Frontend image build failed
    exit /b 1
)
echo [SUCCESS] Frontend image built successfully!
echo.
set /p response="Restart frontend service now? (y/n): "
if /i "!response!"=="y" (
    call %DOCKER_COMPOSE_CMD% up -d --build frontend
    echo [SUCCESS] Frontend service restarted
)
goto :end

:start
echo [INFO] Starting all services...
call %DOCKER_COMPOSE_CMD% up -d
if errorlevel 1 (
    echo [ERROR] Failed to start services
    exit /b 1
) else (
    echo [SUCCESS] Services started successfully!
    echo.
    echo [INFO] Service access addresses:
    echo   Frontend: http://localhost
    echo   Backend: http://localhost:9001
    echo   Redis: localhost:6379
    echo.
    echo [TIP] View logs: %DOCKER_COMPOSE_CMD% logs -f
    echo [TIP] Stop services: %DOCKER_COMPOSE_CMD% down
)
goto :end

:start_backend
echo [INFO] Starting backend service...
call %DOCKER_COMPOSE_CMD% up -d backend
if errorlevel 1 (
    echo [ERROR] Failed to start backend service
    exit /b 1
) else (
    echo [SUCCESS] Backend service started!
)
goto :end

:start_frontend
echo [INFO] Starting frontend service...
call %DOCKER_COMPOSE_CMD% up -d frontend
if errorlevel 1 (
    echo [ERROR] Failed to start frontend service
    exit /b 1
) else (
    echo [SUCCESS] Frontend service started!
)
goto :end

:stop
echo [INFO] Stopping all services...
call %DOCKER_COMPOSE_CMD% down
echo [SUCCESS] Services stopped
goto :end

:stop_backend
echo [INFO] Stopping backend service...
call %DOCKER_COMPOSE_CMD% stop backend
if errorlevel 1 (
    echo [ERROR] Failed to stop backend service
    exit /b 1
) else (
    echo [SUCCESS] Backend service stopped
)
goto :end

:stop_frontend
echo [INFO] Stopping frontend service...
call %DOCKER_COMPOSE_CMD% stop frontend
if errorlevel 1 (
    echo [ERROR] Failed to stop frontend service
    exit /b 1
) else (
    echo [SUCCESS] Frontend service stopped
)
goto :end

:restart
echo [INFO] Restarting all services...
call %DOCKER_COMPOSE_CMD% restart
echo [SUCCESS] Services restarted
goto :end

:restart_backend
echo [INFO] Restarting backend service...
call %DOCKER_COMPOSE_CMD% restart backend
echo [SUCCESS] Backend service restarted
goto :end

:restart_frontend
echo [INFO] Restarting frontend service...
call %DOCKER_COMPOSE_CMD% restart frontend
echo [SUCCESS] Frontend service restarted
goto :end

:logs
if "%SERVICE%"=="" (
    echo [INFO] Viewing all service logs...
    call %DOCKER_COMPOSE_CMD% logs -f
) else (
    echo [INFO] Viewing %SERVICE% service logs...
    call %DOCKER_COMPOSE_CMD% logs -f %SERVICE%
)
goto :end

:clean
echo [INFO] Cleaning Docker resources...
echo [WARNING] This will delete all containers, images and volumes, continue? (y/n)
set /p response=
if /i "!response!"=="y" (
    call %DOCKER_COMPOSE_CMD% down -v
    docker rmi documentgen-backend:latest documentgen-frontend:latest 2>nul
    echo [SUCCESS] Cleanup completed
) else (
    echo [INFO] Cleanup cancelled
)
goto :end

:rebuild
echo [INFO] Rebuilding and starting services...
call %DOCKER_COMPOSE_CMD% down
call %DOCKER_COMPOSE_CMD% build --no-cache
call %DOCKER_COMPOSE_CMD% up -d
echo [SUCCESS] Rebuild and start completed
goto :end

:usage
echo [ERROR] Unknown action: %ACTION%
echo.
echo Usage: %~nx0 [action] [service]
echo.
echo Available actions:
echo   build              - Build all Docker images
echo   build-backend      - Build backend image only
echo   build-frontend     - Build frontend image only
echo   start              - Start all services
echo   start-backend      - Start backend service only
echo   start-frontend     - Start frontend service only
echo   stop               - Stop all services
echo   stop-backend       - Stop backend service only
echo   stop-frontend      - Stop frontend service only
echo   restart            - Restart all services
echo   restart-backend    - Restart backend service only
echo   restart-frontend   - Restart frontend service only
echo   logs [service]     - View service logs (all services if service not specified)
echo   clean              - Clean all Docker resources
echo   rebuild            - Rebuild and start all services
echo.
echo Examples:
echo   %~nx0 build              # Build all images
echo   %~nx0 build-backend      # Build backend only
echo   %~nx0 build-frontend     # Build frontend only
echo   %~nx0 start-backend      # Start backend only
echo   %~nx0 start-frontend     # Start frontend only
echo   %~nx0 stop-backend       # Stop backend only
echo   %~nx0 stop-frontend      # Stop frontend only
echo   %~nx0 restart-backend    # Restart backend only
echo   %~nx0 restart-frontend   # Restart frontend only
echo   %~nx0 logs backend       # View backend logs
exit /b 1

:end
endlocal
