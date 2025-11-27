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
    // Data Modules
    api(project(":im-data"))
    
    // Spring WebFlux
    api(libs.spring.boot.starter.webflux)
    
    // Jackson Kotlin
    implementation(libs.jackson.module.kotlin)
    
    // 测试
    testImplementation(libs.bundles.test)
}

springBoot {
    mainClass.set("com.sudooom.mahjong.web.WebApplicationKt")
}
