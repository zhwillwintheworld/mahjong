package com.sudooom.mahjong.core.config

import com.sudooom.mahjong.common.util.JwtUtil
import io.rsocket.core.RSocketConnector
import io.rsocket.core.Resume
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.protobuf.ProtobufDecoder
import org.springframework.http.codec.protobuf.ProtobufEncoder
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import reactor.util.retry.Retry
import java.time.Duration

/**
 * Broker 连接配置 在应用启动时自动连接到 Broker
 *
 * 使用 Google Protobuf 进行消息编解码 使用 JWT 加密元数据进行安全认证
 */
@Configuration
@EnableConfigurationProperties(BrokerConnectionProperties::class)
@ComponentScan("com.sudooom.mahjong.common")
class BrokerConnectionConfig(
        private val properties: BrokerConnectionProperties,
        private val jwtUtil: JwtUtil,
) {

    /** Broker 连接专用的 RSocketStrategies 使用 Spring 内置的 ProtobufEncoder/ProtobufDecoder */
    @Bean
    fun brokerRSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
            .encoder(ProtobufEncoder())
            .decoder(ProtobufDecoder())
            .build()
    }

    /**
     * 预配置的 RSocketRequester.Builder Bean 包含重连策略、keepAlive 配置和认证信息 实际连接在 BrokerConnectionManager 的
     * @PostConstruct 中建立
     */
    @Bean
    fun brokerRSocketRequesterBuilder(
            brokerRSocketStrategies: RSocketStrategies,
    ): RSocketRequester.Builder {
        // 生成 JWT，包含 instanceType 和 instanceId
        val token =
                jwtUtil.generateServerToken(
                        instanceType = properties.instanceType,
                        instanceId = properties.instanceId
                )

        return RSocketRequester.builder()
                .rsocketStrategies(brokerRSocketStrategies)
                .rsocketConnector { connector: RSocketConnector ->
                    connector
                            .reconnect(
                                    Retry.backoff(
                                        if (properties.maxReconnectAttempts < 0)
                                            Long.MAX_VALUE
                                        else properties.maxReconnectAttempts.toLong(),
                                        Duration.ofMillis(
                                            properties.reconnectIntervalMs
                                        ),
                                    )
                                        .maxBackoff(
                                            Duration.ofMillis(
                                                properties.maxReconnectIntervalMs
                                            )
                                        )
                            )
                            .resume(Resume())
                            .keepAlive(
                                    Duration.ofMillis(properties.keepAliveIntervalMs),
                                    Duration.ofMillis(properties.keepAliveMaxLifetimeMs),
                            )
                }
                .setupRoute(properties.setupRoute)
                .setupData(token)
    }
}
