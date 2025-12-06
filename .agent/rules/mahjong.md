---
trigger: always_on
---

# 项目目录结构

本文档描述了项目的整体目录结构，帮助开发者快速理解代码组织方式。

## 顶级目录结构

```
/
├── docs/                   # 项目文档
├── ops/                    # 运维相关配置
├── env/                    # 开发环境初始化工具
└── project/                # 代码项目目录
    ├── im-server/          # Kotlin后端服务
    ├── im-desktop-web/     # TypeScript桌面端Web项目
    ├── im-mobile-web/      # uniapp移动端Web项目

```

## 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────┐
│                      客户端层                            │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Desktop Web (React 18 + RSocket-JS)             │  │
│  │   - Ant Design 5.x UI 组件                         │  │
│  │   - TypeScript 5.x 类型安全                        │  │
│  │   - Vite 6.x 构建工具                              │  │
│  │   - 通过protobuf发送消息im消息以及接收消息             │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼ (RSocket-Websocket)
┌─────────────────────────────────────────────────────────┐
│                      接入层                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Access Module (有状态) - Kotlin 2.2              │  │
│  │   - WebSocket 长连接管理                            │  │
│  │   - RSocket-WebSocket 协议                         │  │
│  │   - 用户会话维护                                    │  │
│  │   - 向客户端层消息推送                               │  │
│  │   - Kotlin Coroutines 异步处理                     │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼ (RSocket-TCP)
┌─────────────────────────────────────────────────────────┐
│                      转发层                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Broker Module (有状态) - Kotlin 2.2              │  │
│  │   - RSocket-WebSocket 通信                         │  │
│  │   - 转发接入层消息到逻辑层                            │  │
│  │   - 转发逻辑层消息到接入层                            │  │
│  │   - 转发消息时对逻辑层做一致性哈希                     │  │
│  │   - Kotlin Coroutines 异步处理                     │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼ (RSocket-TCP)
┌─────────────────────────────────────────────────────────┐
│                      逻辑层                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Logic Module (无状态) - Kotlin 2.2               │  │
│  │   - protobuf encoder以及decoder                    │  │
│  │   - 业务逻辑处理                                    │  │
│  │   - RSocket-TCP 通信                               │  │
│  │   - 消息路由与分发                                  │  │
│  │   - 数据持久化                                      │  │
│  │   - Kotlin Flow 流式处理                           │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
         ┌────────────────┼────────────────┐
         ▼                ▼                ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Web Module │  │   Redis     │  │  MongoDB    │
│  (REST API) │  │  (缓存)      │  │  (持久化)    │
│   Kotlin    │  │             │  │             │
└─────────────┘  └─────────────┘  └─────────────┘
```

## 项目说明

### docs/
存放项目相关的所有文档，包括设计文档、API文档、使用指南等。文档采用Markdown格式编写。

### ops/
包含运维相关的配置文件和脚本，如Docker配置、CI/CD配置、部署脚本等。

### env/
提供开发环境的初始化和配置工具，帮助开发者快速搭建一致的开发环境。

### project/
代码项目的根目录，包含所有应用程序代码。

#### project/im-server/
Kotlin编写的后端服务，使用Gradle进行构建。负责提供API接口和业务逻辑处理。
所有Gradle构建的根目录在这里，这个目录中有gradlew。
子目录中的Gradle构建项目都是这个目录的子项目，这样可以方便项目间源码依赖。
所有Gradle构建的项目应该使用统一的版本约定，约定文件是project/im-server/gradle/libs.versions.toml。

#### project/im-desktop-web/
TypeScript编写的桌面端Web应用，针对大屏设备优化的用户界面。

#### project/im-mobile-web/
uniapp编写的移动端Web应用，针对移动设备浏览器优化的用户界面。


## 项目开发规范

### project/im-server/
- 作为后端项目的父目录 只负责配置gradle相关

### project/im-server/im-common
- 基础包名：`com.sudooom.mahjong.common`
- 源代码位置：`src/main/kotlin/com/sudooom/mahjong/common/`
- 构建工具：Gradle
- 主要技术栈：Kotlin、Protobuf、jwt

### project/im-server/im-common
- 基础包名：`com.sudooom.mahjong.common`
- 源代码位置：`src/main/kotlin/com/sudooom/mahjong/common/`
- 构建工具：Gradle
- 主要技术栈：Kotlin、Protobuf、jwt

### project/im-server/im-access
- 基础包名：`com.sudooom.mahjong.access`
- 源代码位置：`src/main/kotlin/com/sudooom/mahjong/common/`
- 构建工具：Gradle
- 主要技术栈：Kotlin、springboot4.0、spring-rsocket

### project/im-server/im-broker
- 基础包名：`com.sudooom.mahjong.broker`
- 源代码位置：`src/main/kotlin/com/sudooom/mahjong/broker/`
- 构建工具：Gradle
- 主要技术栈：Kotlin、springboot4.0、spring-rsocket

### project/im-server/im-logic
- 基础包名：`com.sudooom.mahjong.logic`
- 源代码位置：`src/main/kotlin/com/sudooom/mahjong/logic/`
- 构建工具：Gradle
- 主要技术栈：Kotlin、springboot4.0、spring-rsocket 

### project/im-server/im-data
- 基础包名：`com.sudooom.mahjong.data`
- 源代码位置：`src/main/kotlin/com/sudooom/mahjong/data/`
- 构建工具：Gradle
- 主要技术栈：Kotlin、springboot4.0、spring-data-reactive-redis、spring-data-reactive-mongo

### project/desktop-web/
- 主要技术栈：TypeScript、React 、rsocket-js、protobuf
- UI框架：使用Ant Design组件库（`ant-design`）
- 样式方案：优先使用Tailwind CSS，只有在Tailwind难以实现的情况下才使用自定义CSS
- 状态管理：React Context API + Zustand
- 构建工具：Create React App (CRA)

### Kotlin 代码格式化规范
生成 Kotlin 代码时，必须遵循以下格式化规则：
- **基础缩进**：4 空格
- **续行缩进**：4 空格（与基础缩进相同）
- **构造函数参数换行**：每个参数一行，缩进 4 空格
- **链式调用换行**：每个 `.` 操作一行，缩进 4 空格
- **函数参数换行**：每个参数一行，缩进 4 空格
- **空行**：不包含任何空格或缩进字符
- **行尾**：不包含多余空格
示例：
```kotlin
class ExampleClass(
    private val param1: String,
    private val param2: Int,
) {
    fun chainExample() {
        someObject
            .firstCall()
            .secondCall()
            .thirdCall()
    }
}
```