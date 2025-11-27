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
    // Data Modules
    api(project(":im-data"))
    
    // RSocket
    api(libs.spring.boot.starter.rsocket)
    
    // 测试
    testImplementation(libs.bundles.test)
}

springBoot {
    mainClass.set("com.sudooom.mahjong.logic.LogicApplicationKt")
}
