plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.protobuf)
}

dependencies {
    // Kotlin 核心
    api(libs.bundles.kotlin)
    
    // Spring 最小依赖 - 用于 @Component 和 @Value 注解
    api(libs.spring.context)
    api(libs.spring.boot)
    
    // Protobuf Kotlin (传递依赖 protobuf-java)
    api(libs.protobuf.kotlin)
    
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.30.2"
    }
    // 默认会生成 Java 代码，protobuf-kotlin 会提供 Kotlin 扩展
}

sourceSets {
    main {
        proto {
            srcDir("src/main/resources/proto")
        }
    }
}
