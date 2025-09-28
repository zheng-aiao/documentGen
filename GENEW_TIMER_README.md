# Genew文档生成定时器实现

## 功能概述

本实现为GenewDocGenTimer定时器添加了完整的文档生成逻辑，实现了以下功能：

1. **定时执行**：每5分钟执行一次
2. **获取文档列表**：从配置的genew文档服务器获取所有swagger文档信息
3. **双重判断机制**：通过startTime和encrypt值避免重复处理
4. **文档生成**：自动生成API文档并保存到指定目录
5. **Redis缓存**：存储处理状态和文档信息

## 实现细节

### 1. 配置管理
- **GenewConfig**: 读取genew服务器URL和文档输出路径配置
- **application.properties**: 添加了相关配置项

### 2. 核心组件

#### GenewDocGenTimer (定时器)
- 每5分钟执行一次文档生成任务
- 获取swagger文档列表并遍历处理
- 实现双重判断机制避免重复处理

#### GenewRedisService (Redis服务)
- 管理startTime和encrypt集合
- 存储文档信息（版本号、部署时间、来源分支、文档路径）
- 提供检查和处理状态的方法

#### EncryptUtils (加密工具)
- 提供MD5和SHA-256加密算法
- 用于计算swagger JSON数据的唯一标识

#### DocumentPathUtils (文档路径工具)
- 创建文档目录结构：`服务名/版本号/部署时间/文档`
- 生成标准化的文档文件名

### 3. 处理流程

1. **获取文档列表**：从配置的URL获取所有swagger文档信息
2. **遍历处理**：对每个服务进行以下处理：
   - 提取document信息（name、url、startTime等）
   - 第一重判断：检查startTime是否已处理
   - 第二重判断：获取JSON数据，计算encrypt值，检查是否已处理
   - 生成文档：调用现有的文档生成服务
   - 保存状态：将相关信息存储到Redis

### 4. Redis数据结构

- `swagger:genew:startTime:{serviceName}`: Set类型，存储已处理的startTime
- `swagger:genew:encrypt:{serviceName}`: Set类型，存储已处理的encrypt值
- `swagger:genew:document:{serviceName}`: String类型，存储文档信息JSON

### 5. 文档目录结构

```
{basePath}/
├── {serviceName}/
│   ├── {version}/
│   │   ├── {deployTime}/
│   │   │   └── {serviceName}_{version}_API文档.docx
```

## 配置说明

在`application.properties`中添加以下配置：

```properties
# Genew文档服务器配置
genew.server.url=http://localhost:8080/api/genew/swagger/list
genew.document.output.path=./genew-documents
```

## 使用说明

1. 确保Redis服务正常运行
2. 配置正确的genew服务器URL
3. 确保文档输出目录有写入权限
4. 启动应用后，定时器将自动开始工作

## 日志监控

定时器会输出详细的日志信息，包括：
- 任务执行状态
- 文档处理进度
- 错误信息和异常处理
- Redis操作结果

## 注意事项

1. 确保genew服务器返回的JSON格式正确
2. 网络连接异常时会记录错误日志但不影响其他服务处理
3. 文档生成失败时会记录错误但不影响Redis状态更新
4. 建议定期清理Redis中的历史数据以避免内存占用过多
