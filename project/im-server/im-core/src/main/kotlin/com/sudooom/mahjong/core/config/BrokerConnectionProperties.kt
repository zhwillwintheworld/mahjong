package com.sudooom.mahjong.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

/** Broker 连接配置属性 */
@ConfigurationProperties(prefix = "im.broker")
data class BrokerConnectionProperties(
        /** Broker 服务器地址 */
        val host: String = "localhost",

        /** Broker 服务器端口 */
        val port: Int = 7000,

        /** 连接超时时间（毫秒） */
        val connectTimeoutMs: Long = 5000,

        /** 重连间隔时间（毫秒） */
        val reconnectIntervalMs: Long = 3000,

        /** 最大重连次数，-1 表示无限重连 */
        val maxReconnectAttempts: Int = -1,

        /** 心跳间隔时间（毫秒） */
        val keepAliveIntervalMs: Long = 30000,

        /** 心跳超时时间（毫秒） */
        val keepAliveMaxLifetimeMs: Long = 90000,
)
