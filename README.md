# IM 即时通讯系统

基于 Kotlin 2.2 + Spring Boot 4.0 + React 18 构建的现代化即时通讯系统。

## 项目结构

```
.
├── im-server/              # 后端项目（Kotlin）
│   ├── im-common/         # 公共模块
│   ├── im-access/         # 接入层（WebSocket）
│   ├── im-logic/          # 逻辑层（业务处理）
│   └── im-web/            # Web 层（REST API）
└── im-desktop-web/        # 前端项目（React + TypeScript）
```

## 技术栈

### 后端
- **语言**: Kotlin 2.2.20
- **框架**: Spring Boot 4.0.0
- **JDK**: OpenJDK 21 LTS
- **协程**: Kotlin Coroutines 1.10.2
- **通信**: RSocket (WebSocket + TCP)
- **数据库**: MongoDB (响应式)
- **缓存**: Redis (响应式)
- **序列化**: Protobuf 4.30.2
- **构建**: Gradle 8.11.1 (Kotlin DSL)

### 前端
- **框架**: React 18.3.1
- **语言**: TypeScript 5.7.3
- **UI 库**: Ant Design 5.22.6
- **构建**: Vite 6.0.7
- **状态管理**: Zustand 5.0.2
- **通信**: RSocket WebSocket Client
- **包管理**: pnpm 9.15.2

## 快速开始

### 环境要求

- JDK 21+
- Node.js 20+
- pnpm 9+
- Redis 6+
- MongoDB 5+

### 后端启动

```bash
cd im-server

# 1. 复制环境变量
cp .env.example .env

# 2. 构建项目
./gradlew build

# 3. 启动各模块
./gradlew :im-access:bootRun   # 端口 7000
./gradlew :im-logic:bootRun    # 端口 7001
./gradlew :im-web:bootRun      # 端口 8080
```

### 前端启动

```bash
cd im-desktop-web

# 1. 安装依赖
pnpm install

# 2. 启动开发服务器
pnpm dev

# 访问 http://localhost:3000
```

## 模块说明

### im-common（公共模块）
- Protobuf 消息定义
- 常量和枚举类
- 异常定义
- Kotlin 扩展函数
- 工具类（ID 生成器等）

### im-access（接入层）
- 有状态服务
- WebSocket 长连接管理
- 用户会话维护
- 消息实时推送
- 心跳检测

### im-logic（逻辑层）
- 无状态服务
- 业务逻辑处理
- 消息持久化
- 用户管理
- 群组管理

### im-web（Web 层）
- RESTful API
- 用户管理接口
- 历史消息查询
- 文件上传下载

## 端口分配

| 服务 | 端口 | 协议 | 说明 |
|------|------|------|------|
| Access | 7000 | RSocket WebSocket | 用户长连接 |
| Logic | 7001 | RSocket TCP | 业务逻辑 |
| Web | 8080 | HTTP | REST API |
| Frontend | 3000 | HTTP | 前端开发服务器 |
| Redis | 6379 | TCP | 缓存 |
| MongoDB | 27017 | TCP | 数据库 |

## 开发指南

### Gradle 命令

```bash
# 构建所有模块
./gradlew build

# 运行测试
./gradlew test

# 检查依赖更新
./gradlew dependencyUpdates

# 清理构建
./gradlew clean
```

### 前端命令

```bash
# 开发模式
pnpm dev

# 构建生产版本
pnpm build

# 预览生产构建
pnpm preview

# 代码格式化
pnpm format

# 检查依赖更新
pnpm update --interactive --latest
```

## 架构特点

1. **响应式架构**: 全链路响应式，支持高并发
2. **模块化设计**: 后端分层解耦，职责清晰
3. **现代化技术栈**: Kotlin 2.2 + Spring Boot 4.0 + React 18
4. **高效通信**: RSocket 双向通信协议
5. **类型安全**: Kotlin + TypeScript 全栈类型安全
6. **简化部署**: 单体分模块架构，无需微服务复杂度

## 许可证

MIT

## 作者

IM Team
