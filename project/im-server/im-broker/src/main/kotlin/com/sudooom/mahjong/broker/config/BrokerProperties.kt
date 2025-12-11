package com.sudooom.mahjong.broker.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Broker 配置属性
 */
@ConfigurationProperties(prefix = "broker")
data class BrokerProperties(
    /** RSocket 相关配置，预留扩展 */
    val placeholder: String = ""
)
