package com.sudooom.mahjong.access.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler

/**
 * RSocket 配置 - 使用 DataBuffer 实现 zero-copy 转发
 */
@Configuration
class RSocketConfig {

    @Bean
    fun rSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
            // 使用 Netty 作为底层 DataBuffer 实现，确保 zero-copy
            // DataBuffer 默认已有 encoder/decoder 支持，无需额外配置
            .build()
    }

    @Bean
    fun rSocketMessageHandler(rSocketStrategies: RSocketStrategies): RSocketMessageHandler {
        val handler = RSocketMessageHandler()
        handler.rSocketStrategies = rSocketStrategies
        return handler
    }
}
