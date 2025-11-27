
# IM 项目架构设计文档

## 1. 项目概述

本项目是一个基于现代化技术栈的即时通讯（IM）系统，采用前后端分离架构，后端使用 **Kotlin 2.2 + 响应式编程模型**，前端基于 React 18 构建 Web 应用。

### 1.1 技术特点

- **Kotlin 2.2 + 协程**：利用 Kotlin 最新特性，协程的简洁语法，结合响应式流
- **响应式架构**：全链路响应式，支持高并发场景
- **RSocket 通信**：基于 RSocket 协议实现高效双向通信
- **模块化设计**：后端分层解耦，职责清晰
- **现代化前端**：React 18 + TypeScript + Vite 快速构建
- **无需 Spring Cloud**：单体分模块架构，简化部署

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────┐
│                      客户端层                            │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Desktop Web (React 18 + RSocket-JS)             │  │
│  │   - Ant Design 5.x UI 组件                         │  │
│  │   - TypeScript 5.x 类型安全                        │  │
│  │   - Vite 6.x 构建工具                              │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      接入层                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Access Module (有状态) - Kotlin 2.2              │  │
│  │   - WebSocket 长连接管理                           │  │
│  │   - RSocket-WebSocket 协议                         │  │
│  │   - 用户会话维护                                    │  │
│  │   - 消息推送                                        │  │
│  │   - Kotlin Coroutines 异步处理                     │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼ (RSocket-TCP)
┌─────────────────────────────────────────────────────────┐
│                      逻辑层                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Logic Module (无状态) - Kotlin 2.2               │  │
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

### 2.2 模块职责

#### **Access 模块（接入层）**
- **角色**：有状态服务，维护用户长连接
- **职责**：
  - 用户 WebSocket 连接管理
  - 在线状态维护
  - 消息实时推送
  - 心跳检测
- **技术栈**：Spring Boot 4.0 + Kotlin 2.2 + WebFlux + RSocket-WebSocket + Coroutines 1.10
- **特点**：
  - 有状态，可水平扩展
  - 使用 Redis 存储用户路由信息
  - Kotlin 协程处理并发连接

#### **Logic 模块（逻辑层）**
- **角色**：无状态服务，处理业务逻辑
- **职责**：
  - 消息业务逻辑处理
  - 用户认证与授权
  - 群组管理
  - 消息存储与查询
  - 消息路由决策
- **技术栈**：Spring Boot 4.0 + Kotlin 2.2 + WebFlux + RSocket-TCP + Coroutines 1.10
- **特点**：
  - 无状态，易于扩展
  - 通过 RSocket-TCP 与 Access 通信
  - Kotlin Flow 处理数据流

#### **Web 模块（REST 接口层）**
- **角色**：对外提供 HTTP REST API
- **职责**：
  - 用户管理 API
  - 群组管理 API
  - 历史消息查询
  - 文件上传下载
- **技术栈**：Spring Boot 4.0 + Kotlin 2.2 + WebFlux（Netty 自动依赖）
- **特点**：
  - 无状态 RESTful 服务
  - 用于非实时场景
  - Kotlin DSL 路由配置

#### **Common 模块（公共模块）**
- **角色**：公共代码库
- **职责**：
  - 通用工具类（Kotlin 扩展函数）
  - Protobuf 消息定义
  - 常量与枚举
  - 异常定义
  - 响应式工具类
  - Kotlin DSL 构建器
- **特点**：被其他模块依赖

---

## 3. 技术栈详解

### 3.1 后端技术栈

#### **核心框架**
```yaml
语言: Kotlin 2.2.20 (最新稳定版)
JDK: OpenJDK 21 LTS
框架: Spring Boot 4.0.0 (2025年11月20日发布)
响应式: Spring WebFlux (内置 Netty 5.x)
协程: Kotlin Coroutines 1.10.2 (最新稳定版)
RSocket: 
  - Spring RSocket (随 Spring Boot 4.0 提供)
  - Access: RSocket-WebSocket
  - Logic: RSocket-TCP
```

#### **Kotlin 2.2 核心特性**
```kotlin
// Context receivers (Kotlin 2.2 特性)
context(CoroutineScope)
suspend fun sendMessage(message: Message) {
    launch {
        // 异步发送
    }
}

// Flow 流式处理
fun getMessageStream(): Flow<Message> = flow {
    // 持续发送消息流
}

// DSL 构建器
fun buildMessage(block: MessageBuilder.() -> Unit): Message {
    return MessageBuilder().apply(block).build()
}

// 扩展函数
fun String.toUserId(): UserId = UserId(this)

// 数据类与密封类
data class Message(val id: String, val content: String)

sealed interface ConnectionState {
    data object Connected : ConnectionState
    data object Disconnected : ConnectionState
    data class Error(val message: String) : ConnectionState
}

// Guards 语句 (Kotlin 2.2)
fun processMessage(msg: Message?) {
    guard(msg != null) { return }
    // msg 在此已智能转型为非空
}
```

#### **数据存储**
```yaml
Redis: 
  - spring-boot-starter-data-redis-reactive (随 Spring Boot 4.0)
  - Lettuce 6.5+ (自动依赖，支持 Coroutines)
  - 用途: 用户会话、在线状态、消息缓存
  
MongoDB:
  - spring-boot-starter-data-mongodb-reactive (随 Spring Boot 4.0)
  - MongoDB Driver 5.x (自动依赖)
  - 用途: 消息持久化、用户数据、群组数据
```

#### **序列化协议**
```yaml
Protobuf:
  - protobuf-kotlin 4.30.2 (最新，修复 CVE-2024-7254)
  - Kotlin DSL 构建消息
  - 用于 RSocket 消息传输
  - 支持 Protobuf Edition 2024
```

#### **构建工具**
```yaml
Gradle: 8.11.1 (支持 Kotlin 2.2)
Gradle Kotlin DSL: 
  - build.gradle.kts
  - settings.gradle.kts
  - 类型安全的构建脚本
  - 多模块项目管理
  - 版本目录 (libs.versions.toml)
```

### 3.2 前端技术栈

```yaml
核心框架:
  - React: 18.3.1 (最新稳定版)
  - TypeScript: 5.7.3 (最新)

UI 组件库:
  - Ant Design: 5.22.6 (最新)

构建工具:
  - Vite: 6.0.7 (最新)

通信协议:
  - rsocket-core: 1.0.3 (最新)
  - rsocket-websocket-client: 1.0.3

状态管理:
  - zustand: 5.0.2 (最新)

包管理:
  - pnpm: 9.15.2 (推荐，比 npm 快 2-3 倍)
```

---

## 4. 项目结构

### 4.1 后端项目结构（Kotlin 2.2）

```
im-server/
├── build.gradle.kts                  # 根 Gradle 配置 (Kotlin DSL)
├── settings.gradle.kts               # 多模块配置 (Kotlin DSL)
├── gradle.properties                 # Gradle 属性
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml           # 版本目录（集中管理依赖版本）
│
├── im-common/                        # 公共模块
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/com/im/common/
│       │   ├── constant/            # 常量定义
│       │   │   └── Constants.kt
│       │   ├── enums/               # 枚举定义
│       │   │   └── MessageType.kt
│       │   ├── exception/           # 异常定义
│       │   │   ├── IMException.kt
│       │   │   └── ErrorCode.kt
│       │   ├── extension/           # Kotlin 扩展函数
│       │   │   ├── StringExt.kt
│       │   │   ├── FlowExt.kt
│       │   │   └── CoroutineExt.kt
│       │   ├── util/                # 工具类
│       │   │   ├── SnowflakeIdGenerator.kt
│       │   │   └── JsonUtil.kt
│       │   ├── proto/               # Protobuf Kotlin 生成代码
│       │   │   └── MessageProtos.kt
│       │   ├── dsl/                 # DSL 构建器
│       │   │   └── MessageDsl.kt
│       │   └── model/               # 通用数据模型
│       │       └── Result.kt
│       └── resources/
│           └── proto/               # .proto 文件
│               └── message.proto
│
├── im-access/                        # 接入层模块
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/com/im/access/
│       │   ├── AccessApplication.kt # 启动类
│       │   ├── config/              # 配置类
│       │   │   ├── RSocketConfig.kt
│       │   │   ├── RedisConfig.kt
│       │   │   └── CoroutineConfig.kt
│       │   ├── controller/          # RSocket Controller
│       │   │   ├── MessageController.kt
│       │   │   └── HeartbeatController.kt
│       │   ├── service/             # 业务服务
│       │   │   ├── ConnectionService.kt
│       │   │   ├── MessagePushService.kt
│       │   │   └── impl/
│       │   ├── handler/             # 消息处理器
│       │   │   └── MessageHandler.kt
│       │   ├── session/             # 会话管理
│       │   │   ├── UserSession.kt
│       │   │   └── SessionManager.kt
│       │   └── extension/           # 模块扩展函数
│       │       └── RSocketExt.kt
│       └── resources/
│           └── application.yml
│
├── im-logic/                         # 逻辑层模块
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/com/im/logic/
│       │   ├── LogicApplication.kt  # 启动类
│       │   ├── config/              # 配置类
│       │   │   ├── RSocketConfig.kt
│       │   │   ├── MongoConfig.kt
│       │   │   ├── RedisConfig.kt
│       │   │   └── CoroutineConfig.kt
│       │   ├── service/             # 业务服务
│       │   │   ├── MessageService.kt
│       │   │   ├── UserService.kt
│       │   │   ├── GroupService.kt
│       │   │   └── impl/
│       │   ├── repository/          # 数据访问层
│       │   │   ├── MessageRepository.kt
│       │   │   ├── UserRepository.kt
│       │   │   └── GroupRepository.kt
│       │   ├── model/               # 数据模型
│       │   │   ├── entity/          # 实体类
│       │   │   │   ├── Message.kt
│       │   │   │   ├── User.kt
│       │   │   │   └── Group.kt
│       │   │   └── dto/             # DTO
│       │   ├── handler/             # RSocket 处理器
│       │   │   └── LogicMessageHandler.kt
│       │   ├── router/              # 消息路由
│       │   │   └── MessageRouter.kt
│       │   └── extension/           # 模块扩展函数
│       │       ├── FlowExt.kt
│       │       └── MongoExt.kt
│       └── resources/
│           └── application.yml
│
└── im-web/                           # Web 接口模块
    ├── build.gradle.kts
    └── src/main/
        ├── kotlin/com/im/web/
        │   ├── WebApplication.kt    # 启动类
        │   ├── config/              # 配置类
        │   │   ├── WebFluxConfig.kt
        │   │   ├── RouterConfig.kt  # Kotlin DSL 路由
        │   │   └── CorsConfig.kt
        │   ├── controller/          # REST Controller (注解式)
        │   │   ├── UserController.kt
        │   │   ├── GroupController.kt
        │   │   └── MessageController.kt
        │   ├── handler/             # Handler (函数式路由)
        │   │   ├── UserHandler.kt
        │   │   └── MessageHandler.kt
        │   ├── dto/                 # 数据传输对象
        │   │   ├── request/
        │   │   │   ├── LoginRequest.kt
        │   │   │   └── SendMessageRequest.kt
        │   │   └── response/
        │   │       ├── ApiResponse.kt
        │   │       └── MessageResponse.kt
        │   ├── service/             # 业务服务
        │   │   └── ApiService.kt
        │   ├── filter/              # 过滤器
        │   │   └── AuthFilter.kt
        │   └── extension/           # 模块扩展函数
        │       └── ServerRequestExt.kt
        └── resources/
            └── application.yml
```

### 4.2 前端项目结构

```
im-desktop-web/
├── package.json                      # npm 依赖配置
├── pnpm-lock.yaml                    # 锁定依赖版本（推荐）
├── .npmrc                            # pnpm 配置
├── vite.config.ts                    # Vite 配置
├── tsconfig.json                     # TypeScript 配置
├── tsconfig.node.json                # Node TypeScript 配置
├── index.html                        # 入口 HTML
├── .gitignore
│
├── public/                           # 静态资源
│   └── favicon.ico
│
└── src/
    ├── main.tsx                      # 应用入口
    ├── App.tsx                       # 根组件
    ├── App.css
    │
    ├── api/                          # API 层
    │   ├── rsocket.ts               # RSocket 客户端
    │   ├── rest.ts                  # REST API
    │   └── proto/                   # Protobuf 生成代码
    │
    ├── components/                   # 组件
    │   ├── ChatWindow/
    │   │   ├── index.tsx
    │   │   └── style.css
    │   ├── MessageList/
    │   │   ├── index.tsx
    │   │   └── style.css
    │   ├── ContactList/
    │   │   ├── index.tsx
    │   │   └── style.css
    │   └── Common/
    │       └── index.tsx
    │
    ├── pages/                        # 页面
    │   ├── Login/
    │   │   ├── index.tsx
    │   │   └── style.css
    │   ├── Chat/
    │   │   ├── index.tsx
    │   │   └── style.css
    │   └── Settings/
    │       ├── index.tsx
    │       └── style.css
    │
    ├── hooks/                        # 自定义 Hooks
    │   ├── useRSocket.ts
    │   └── useMessageHandler.ts
    │
    ├── store/                        # 状态管理 (Zustand)
    │   ├── userStore.ts
    │   ├── messageStore.ts
    │   └── index.ts
    │
    ├── types/                        # TypeScript 类型定义
    │   ├── message.ts
    │   ├── user.ts
    │   └── index.ts
    │
    ├── utils/                        # 工具函数
    │   ├── protobuf.ts
    │   └── format.ts
    │
    └── styles/                       # 样式文件
        └── global.css
```

---

## 5. 依赖管理配置

### 5.1 后端依赖管理（Gradle Kotlin DSL）

#### **根 build.gradle.kts**

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20" apply false
    kotlin("plugin.spring") version "2.2.20" apply false
    id("org.springframework.boot") version "4.0.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.google.protobuf") version "0.9.4" apply false
    id("com.github.ben-manes.versions") version "0.51.0" // 依赖版本检查
}

allprojects {
    group = "com.im"
    version = "1.0.0-SNAPSHOT"
    
    repositories {
        mavenCentral()
        maven("https://repo.spring.io/milestone")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xcontext-receivers", // Kotlin 2.2 特性
                "-opt-in=kotlin.RequiresOptIn"
            )
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// 依赖更新检查配置
tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { 
        version.uppercase().contains(it) 
    }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return !stableKeyword && !version.matches(regex)
}
```

#### **settings.gradle.kts**

```kotlin
rootProject.name = "im-server"

include(
    "im-common",
    "im-access",
    "im-logic",
    "im-web"
)
```

#### **gradle.properties**

```properties
# Gradle
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true

# Kotlin
kotlin.code.style=official
kotlin.incremental=true
kotlin.stdlib.default.dependency=false
```

#### **gradle/libs.versions.toml（版本目录 - 最新版本）**

```toml
[versions]
# 核心
kotlin = "2.2.20"
spring-boot = "4.0.0"
kotlin-coroutines = "1.10.2"

# Protobuf
protobuf = "4.30.2"

# Jackson
jackson = "2.18.2"

# 测试
mockk = "1.13.14"
kotest = "5.9.1"

[libraries]
# Kotlin 核心
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlin-reflect = { module = "org.jetbrains.kotlin:kotlin-reflect", version.ref = "kotlin" }
kotlin-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlin-coroutines" }
kotlin-coroutines-reactor = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor", version.ref = "kotlin-coroutines" }
kotlin-coroutines-jdk9 = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk9", version.ref = "kotlin-coroutines" }

# Spring Boot Starters (版本由 Spring Boot 管理)
spring-boot-starter-webflux = { module = "org.springframework.boot:spring-boot-starter-webflux" }
spring-boot-starter-rsocket = { module = "org.springframework.boot:spring-boot-starter-rsocket" }
spring-boot-starter-data-redis-reactive = { module = "org.springframework.boot:spring-boot-starter-data-redis-reactive" }
spring-boot-starter-data-mongodb-reactive = { module = "org.springframework.boot:spring-boot-starter-data-mongodb-reactive" }

# Protobuf Kotlin
protobuf-kotlin = { module = "com.google.protobuf:protobuf-kotlin", version.ref = "protobuf" }

# Jackson Kotlin
jackson-module-kotlin = { module = "com.fasterxml.jackson.module:jackson-module-kotlin", version.ref = "jackson" }

# 工具类
commons-lang3 = { module = "org.apache.commons:commons-lang3", version = "3.17.0" }

# 测试
spring-boot-starter-test = { module = "org.springframework.boot:spring-boot-starter-test" }
kotlin-test-junit5 = { module = "org.jetbrains.kotlin:kotlin-test-junit5", version.ref = "kotlin" }
reactor-test = { module = "io.projectreactor:reactor-test" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
kotest-runner = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }

[bundles]
kotlin = [
    "kotlin-stdlib",
    "kotlin-reflect",
    "kotlin-coroutines-core",
    "kotlin-coroutines-reactor",
    "kotlin-coroutines-jdk9"
]
spring-reactive = [
    "spring-boot-starter-webflux",
    "spring-boot-starter-rsocket"
]
data-store = [
    "spring-boot-starter-data-redis-reactive",
    "spring-boot-starter-data-mongodb-reactive"
]
test = [
    "spring-boot-starter-test",
    "kotlin-test-junit5",
    "reactor-test",
    "mockk",
    "kotest-runner",
    "kotest-assertions"
]

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-spring = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }
protobuf = { id = "com.google.protobuf", version = "0.9.4" }
```

#### **im-common/build.gradle.kts**

```kotlin
plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.protobuf)
}

dependencies {
    // Kotlin
    api(libs.bundles.kotlin)
    
    // Protobuf Kotlin
    api(libs.protobuf.kotlin)
    
    // Spring Reactive (不需要 Boot)
    api(libs.spring.boot.starter.webflux)
    
    // Jackson Kotlin
    api(libs.jackson.module.kotlin)
    
    // 工具类
    implementation(libs.commons.lang3)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("kotlin") {
                    // 使用 Kotlin 生成代码
                }
            }
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("src/main/resources/proto")
        }
    }
}
```

#### **im-access/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // 依赖 Common 模块
    implementation(project(":im-common"))
    
    // Kotlin
    implementation(libs.bundles.kotlin)
    
    // Spring Reactive + RSocket
    implementation(libs.bundles.spring.reactive)
    implementation(libs.spring.boot.starter.data.redis.reactive)
    
    // Netty 由 WebFlux 自动引入，无需手动添加
    
    // 测试
    testImplementation(libs.bundles.test)
}

springBoot {
    mainClass.set("com.im.access.AccessApplicationKt")
}
```

#### **im-logic/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // 依赖 Common 模块
    implementation(project(":im-common"))
    
    // Kotlin
    implementation(libs.bundles.kotlin)
    
    // Spring Reactive + RSocket + 数据存储
    implementation(libs.bundles.spring.reactive)
    implementation(libs.bundles.data.store)
    
    // 测试
    testImplementation(libs.bundles.test)
}

springBoot {
    mainClass.set("com.im.logic.LogicApplicationKt")
}
```

#### **im-web/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // 依赖 Common 模块
    implementation(project(":im-common"))
    
    // Kotlin
    implementation(libs.bundles.kotlin)
    
    // Spring WebFlux (不需要 RSocket)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    
    // Jackson Kotlin
    implementation(libs.jackson.module.kotlin)
    
    // 测试
    testImplementation(libs.bundles.test)
}

springBoot {
    mainClass.set("com.im.web.WebApplicationKt")
}
```

### 5.2 前端依赖管理（pnpm）

#### **package.json**

```json
{
  "name": "im-desktop-web",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx",
    "format": "prettier --write \"src/**/*.{ts,tsx,css}\"",
    "update": "pnpm update --latest"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "antd": "^5.22.6",
    "rsocket-core": "^1.0.3",
    "rsocket-websocket-client": "^1.0.3",
    "rsocket-flowable": "^0.0.19",
    "protobufjs": "^7.4.0",
    "zustand": "^5.0.2",
    "react-router-dom": "^7.1.1",
    "axios": "^1.7.9",
    "dayjs": "^1.11.13"
  },
  "devDependencies": {
    "@types/react": "^18.3.18",
    "@types/react-dom": "^18.3.5",
    "@vitejs/plugin-react": "^4.3.4",
    "typescript": "^5.7.3",
    "vite": "^6.0.7",
    "eslint": "^9.17.0",
    "@typescript-eslint/eslint-plugin": "^8.19.1",
    "@typescript-eslint/parser": "^8.19.1",
    "prettier": "^3.4.2"
  },
  "engines": {
    "node": ">=20.0.0",
    "pnpm": ">=9.0.0"
  }
}
```

#### **.npmrc**

```properties
# 使用 pnpm
auto-install-peers=true
strict-peer-dependencies=false

# 锁定版本前缀
save-exact=false
save-prefix=^

# 镜像配置（可选）
# registry=https://registry.npmmirror.com
```

#### **tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2023", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,

    /* Bundler mode */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",

    /* Linting */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,

    /* Path mapping */
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

#### **vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

---

## 6. 快速启动脚本

### 6.1 后端初始化脚本

创建 `init-project.sh`（Linux/macOS）：

```bash
#!/bin/bash

echo "初始化 IM 项目..."

# 创建项目目录结构
mkdir -p im-server/{im-common,im-access,im-logic,im-web}/{src/main/{kotlin,resources},src/test/kotlin}

# 创建 Common 模块子目录
mkdir -p im-server/im-common/src/main/kotlin/com/im/common/{constant,enums,exception,extension,util,proto,dsl,model}
mkdir -p im-server/im-common/src/main/resources/proto

# 创建 Access 模块子目录
mkdir -p im-server/im-access/src/main/kotlin/com/im/access/{config,controller,service/impl,handler,session,extension}

# 创建 Logic 模块子目录
mkdir -p im-server/im-logic/src/main/kotlin/com/im/logic/{config,service/impl,repository,model/{entity,dto},handler,router,extension}

# 创建 Web 模块子目录
mkdir -p im-server/im-web/src/main/kotlin/com/im/web/{config,controller,handler,dto/{request,response},service,filter,extension}

# 创建 Gradle wrapper 目录
mkdir -p im-server/gradle/wrapper

echo "✅ 项目结构创建完成！"
echo "下一步："
echo "1. 复制 Gradle 配置文件"
echo "2. 执行: cd im-server && ./gradlew build"
```

创建 `init-project.bat`（Windows）：

```batch
@echo off
echo 初始化 IM 项目...

REM 创建项目目录结构
mkdir im-server\im-common\src\main\kotlin\com\im\common
mkdir im-server\im-common\src\main\resources\proto
mkdir im-server\im-common\src\test\kotlin

mkdir im-server\im-access\src\main\kotlin\com\im\access
mkdir im-server\im-access\src\main\resources
mkdir im-server\im-access\src\test\kotlin

mkdir im-server\im-logic\src\main\kotlin\com\im\logic
mkdir im-server\im-logic\src\main\resources
mkdir im-server\im-logic\src\test\kotlin

mkdir im-server\im-web\src\main\kotlin\com\im\web
mkdir im-server\im-web\src\main\resources
mkdir im-server\im-web\src\test\kotlin

mkdir im-server\gradle\wrapper

echo ✅ 项目结构创建完成！
pause
```

### 6.2 前端初始化脚本

```bash
#!/bin/bash

echo "初始化前端项目..."

# 使用 Vite 创建项目
pnpm create vite im-desktop-web --template react-ts

cd im-desktop-web

# 安装依赖
pnpm install

# 安装额外依赖
pnpm add antd rsocket-core rsocket-websocket-client rsocket-flowable protobufjs zustand react-router-dom axios dayjs

# 安装开发依赖
pnpm add -D prettier

# 创建目录结构
mkdir -p src/{api,components,pages,hooks,store,types,utils,styles}
mkdir -p src/api/proto
mkdir -p src/components/{ChatWindow,MessageList,ContactList,Common}
mkdir -p src/pages/{Login,Chat,Settings}

echo "✅ 前端项目初始化完成！"
echo "下一步："
echo "1. cd im-desktop-web"
echo "2. pnpm dev"
```

---

## 7. 依赖自动更新

### 7.1 Gradle 依赖更新

```bash
# 检查所有可更新的依赖
./gradlew dependencyUpdates

# 输出到文件
./gradlew dependencyUpdates > dependency-updates.txt
```

### 7.2 前端依赖更新

```bash
# 检查可更新的依赖
pnpm outdated

# 交互式更新
pnpm update --interactive --latest

# 自动更新到最新版本
pnpm update --latest
```

### 7.3 自动化更新（GitHub Actions）

创建 `.github/workflows/dependency-update.yml`：

```yaml
name: Dependency Update Check

on:
  schedule:
    - cron: '0 0 * * 1'  # 每周一检查
  workflow_dispatch:

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'
      
      - name: Check Gradle dependencies
        run: |
          cd im-server
          ./gradlew dependencyUpdates
      
      - name: Upload report
        uses: actions/upload-artifact@v4
        with:
          name: gradle-dependency-updates
          path: im-server/build/dependencyUpdates/

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
      
      - name: Setup pnpm
        uses: pnpm/action-setup@v2
        with:
          version: 9
      
      - name: Check npm dependencies
        run: |
          cd im-desktop-web
          pnpm outdated
```

---

## 8. 端口分配

```yaml
开发环境:
  - Access RSocket: 7000
  - Logic RSocket: 7001
  - Web HTTP: 8080
  - Frontend Dev: 3000
  - Redis: 6379
  - MongoDB: 27017

生产环境:
  - Access RSocket: 7000
  - Logic RSocket: 7001
  - Web HTTP: 8080
  - Frontend: 80 (Nginx)
  - Redis: 6379
  - MongoDB: 27017
```

---

## 9. 下一步计划

1. ✅ **项目结构搭建**（本文档）
2. ⏭️ **实现 Common 模块**（Protobuf 定义、工具类）
3. ⏭️ **实现 Access 模块**（WebSocket 连接管理）
4. ⏭️ **实现 Logic 模块**（业务逻辑处理）
5. ⏭️ **实现 Web 模块**（REST API）
6. ⏭️ **前端基础框架**（路由、状态管理）
7. ⏭️ **前后端联调**
8. ⏭️ **性能优化与测试**

---

## 附录

### A. 参考文档

- [Spring Boot 4.0 Documentation](https://docs.spring.io/spring-boot/docs/4.0.0/reference/)
- [Kotlin 2.2 Documentation](https://kotlinlang.org/docs/whatsnew22.html)
- [RSocket Protocol](https://rsocket.io/)
- [React 18 Documentation](https://react.dev/)
- [Ant Design Documentation](https://ant.design/)
- [Vite Documentation](https://vitejs.dev/)

### B. 环境变量模板

创建 `im-server/.env.example`：

```properties
# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# MongoDB
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=im
MONGODB_USERNAME=
MONGODB_PASSWORD=

# 服务端口
ACCESS_PORT=7000
LOGIC_PORT=7001
WEB_PORT=8080
```

### C. 版本记录

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 1.0.0 | 2025-11-27 | 初始版本，完整脚手架设计 |

