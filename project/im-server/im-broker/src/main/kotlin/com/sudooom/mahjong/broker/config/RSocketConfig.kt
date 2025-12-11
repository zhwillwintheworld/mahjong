package com.sudooom.mahjong.broker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.codec.DataBufferDecoder
import org.springframework.core.codec.DataBufferEncoder
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler

/** RSocket 配置 Broker 只负责转发二进制数据，不需要解析 payload */
@Configuration
class RSocketConfig {

    @Bean
    fun rSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
            .encoder(DataBufferEncoder())
            .decoder(DataBufferDecoder())
            .build()
    }

    @Bean
    fun rSocketMessageHandler(rSocketStrategies: RSocketStrategies): RSocketMessageHandler {
        val handler = RSocketMessageHandler()
        handler.rSocketStrategies = rSocketStrategies
        return handler
    }
}
