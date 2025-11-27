plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Spring Boot BOM
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // Common 模块
    api(project(":im-common"))
    
    // Redis Reactive (Optional)
    api(libs.spring.boot.starter.data.redis.reactive)
    
    // MongoDB Reactive (Optional)
    api(libs.spring.boot.starter.data.mongodb.reactive)
}
