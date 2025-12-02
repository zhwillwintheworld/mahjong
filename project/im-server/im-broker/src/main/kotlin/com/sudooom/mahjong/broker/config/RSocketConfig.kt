package com.sudooom.mahjong.broker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler

/**
 * RSocket 配置
 */
@Configuration
class RSocketConfig {

    @Bean
    fun rSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
            .encoders { it.add(org.springframework.http.codec.protobuf.ProtobufEncoder()) }
            .decoders { it.add(org.springframework.http.codec.protobuf.ProtobufDecoder()) }
            .build()
    }
    
    @Bean
    fun rSocketMessageHandler(rSocketStrategies: RSocketStrategies): RSocketMessageHandler {
        val handler = RSocketMessageHandler()
        handler.rSocketStrategies = rSocketStrategies
        return handler
    }
}
