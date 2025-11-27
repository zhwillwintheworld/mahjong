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
    
    // Spring Reactive    // Data Modules
    api(project(":im-data"))
    
    // RSocket
    api(libs.spring.boot.starter.rsocket)
    
    // Netty 由 WebFlux 自动引入，无需手动添加
    
    // 测试
    testImplementation(libs.bundles.test)
}

springBoot {
    mainClass.set("com.sudooom.mahjong.access.AccessApplicationKt")
}
