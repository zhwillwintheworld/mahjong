import com.google.protobuf.gradle.id

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Kotlin 核心
    api(libs.bundles.kotlin)
    
    // Spring 最小依赖 - 用于 @Component 和 @Value 注解
    api(libs.spring.context)
    api(libs.spring.boot)
    api(libs.protobuf.java)
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

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0")
    }
}


// 4. 配置 Protobuf 编译任务
protobuf {
    // 指定 protoc 编译器（插件会自动下载，不需要本地安装）
    protoc {
        artifact = "com.google.protobuf:protoc:4.30.2"
    }

    // 配置生成规则
    generateProtoTasks {
        all().forEach { task ->
            // 内置支持 Java 和 Kotlin
            task.builtins {
                // 生成 Java 代码 (基础，必须要有，因为 Kotlin 扩展依赖它)
                // 生成 Kotlin DSL 代码 (这是你要的)
                id("kotlin")
            }
        }
    }
}

// 5. 将生成的代码路径加入 SourceSet，让 IDE 能识别
sourceSets {
    main {
        proto {
            srcDir("src/main/resources/proto")
        }
        java {
            srcDirs("build/generated/source/proto/main/java")
            srcDirs("build/generated/source/proto/main/kotlin")
        }
    }
}
