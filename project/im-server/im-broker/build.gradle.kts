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
    
    // RSocket
    api(libs.spring.boot.starter.rsocket)
    
    // 测试
    testImplementation(libs.bundles.test)
}

springBoot {
    mainClass.set("com.sudooom.mahjong.broker.BrokerApplicationKt")
}
