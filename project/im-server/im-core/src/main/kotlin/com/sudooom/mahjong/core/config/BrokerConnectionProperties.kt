package com.sudooom.mahjong.core.config

import com.sudooom.mahjong.common.util.IdGenerator
import org.springframework.boot.context.properties.ConfigurationProperties

/** Broker 连接配置属性 */
@ConfigurationProperties(prefix = "im.broker")
data class BrokerConnectionProperties(
        /** Broker 服务器地址 */
        val host: String = "localhost",

        /** Broker 服务器端口 */
        val port: Int = 7000,

        /** 连接建立时的 setup route */
        val setupRoute: String = "broker.connect",

        /** 实例 ID，用于 setup metadata，唯一标识当前实例 */
        val instanceId: String = IdGenerator.nextIdString(),

        /** 实例类型，用于 setup metadata，标识当前实例类型（ACCESS/LOGIC） */
        val instanceType: String = "UNKNOWN",

        /** request-channel 路由 */
        val channelRoute: String = "broker.channel",

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
