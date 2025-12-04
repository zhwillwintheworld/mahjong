plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.wire)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Kotlin 核心
    api(libs.bundles.kotlin)
    
    // Spring 最小依赖 - 用于 @Component 和 @Value 注解
    api(libs.spring.context)
    api(libs.spring.boot)
    
    // Wire Runtime
    api(libs.wire.runtime)
    
    // Jackson Kotlin - JSON 序列化
    api(libs.jackson.module.kotlin)
    
    // JWT - Token 生成和验证
    api(libs.bundles.jwt)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    
    // 工具类
    implementation(libs.commons.lang3)
    
    // SLF4J - 日志接口（不依赖具体实现）
    api(libs.slf4j.api)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0")
    }
}

wire {
    sourcePath {
        srcDir("src/main/resources/proto")
    }
    
    kotlin {
        // 默认生成 builder 模式，如果喜欢数据类可以配置 android = true (虽然名字叫 android 但其实是生成 data class)
        // 或者使用 javaInterop = true 等
        // 这里使用默认配置，生成 Kotlin 类
        out = "src/main/kotlin"
    }
}
