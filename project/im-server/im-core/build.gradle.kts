plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

// im-core 是库模块，不需要打包成可执行 jar
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

dependencies {
    // 依赖 Common 模块
    api(project(":im-common"))

    // Kotlin
    implementation(libs.bundles.kotlin)

    // RSocket Client
    api(libs.spring.boot.starter.rsocket)

    // 测试
    testImplementation(libs.bundles.test)
}
