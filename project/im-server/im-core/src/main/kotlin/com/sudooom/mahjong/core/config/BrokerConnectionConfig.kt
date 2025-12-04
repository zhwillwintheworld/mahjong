package com.sudooom.mahjong.core.config

import io.rsocket.core.RSocketConnector
import io.rsocket.core.Resume
import java.time.Duration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.protobuf.ProtobufDecoder
import org.springframework.http.codec.protobuf.ProtobufEncoder
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import reactor.util.retry.Retry

/** Broker 连接配置 在应用启动时自动连接到 Broker */
@Configuration
@EnableConfigurationProperties(BrokerConnectionProperties::class)
class BrokerConnectionConfig(
        private val properties: BrokerConnectionProperties,
) {

    @Bean
    fun rSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
                .encoders { it.add(ProtobufEncoder()) }
                .decoders { it.add(ProtobufDecoder()) }
                .build()
    }

    @Bean
    fun brokerRSocketRequester(
            rSocketStrategies: RSocketStrategies,
            builder: RSocketRequester.Builder,
    ): RSocketRequester {
        return builder.rsocketStrategies(rSocketStrategies)
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
                .tcp(properties.host, properties.port)
    }
}
