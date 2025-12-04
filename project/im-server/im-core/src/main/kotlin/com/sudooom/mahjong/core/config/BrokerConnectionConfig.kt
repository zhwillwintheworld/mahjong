package com.sudooom.mahjong.core.config

import io.rsocket.core.RSocketConnector
import io.rsocket.core.Resume
import java.time.Duration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.util.MimeType
import reactor.util.retry.Retry

/**
 * Broker 连接配置 在应用启动时自动连接到 Broker
 *
 * 连接使用默认的二进制 encoder/decoder，不解析 payload 内容
 */
@Configuration
@EnableConfigurationProperties(BrokerConnectionProperties::class)
class BrokerConnectionConfig(
        private val properties: BrokerConnectionProperties,
) {

    /** Broker 连接专用的 RSocketStrategies 使用默认的二进制 encoder/decoder，不需要 protobuf */
    @Bean
    fun brokerRSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder().build()
    }

    /** Broker RSocket Requester 配置 setupRoute 和 setupMetadata，使用默认二进制策略 */
    @Bean
    fun brokerRSocketRequester(
            brokerRSocketStrategies: RSocketStrategies,
    ): RSocketRequester {
        return RSocketRequester.builder()
                .rsocketStrategies(brokerRSocketStrategies)
                .rsocketConnector { connector: RSocketConnector ->
                    connector
                            .reconnect(
                                    Retry.backoff(
                                            if (properties.maxReconnectAttempts < 0) Long.MAX_VALUE
                                            else properties.maxReconnectAttempts.toLong(),
                                            Duration.ofMillis(properties.reconnectIntervalMs),
                                    )
                            )
                            .resume(Resume())
                            .keepAlive(
                                    Duration.ofMillis(properties.keepAliveIntervalMs),
                                    Duration.ofMillis(properties.keepAliveMaxLifetimeMs),
                            )
                }
                .setupRoute(properties.setupRoute)
                .setupMetadata(
                        "${properties.instanceType}:${properties.instanceId}",
                        MimeType.valueOf("text/plain")
                )
                .tcp(properties.host, properties.port)
    }
}
