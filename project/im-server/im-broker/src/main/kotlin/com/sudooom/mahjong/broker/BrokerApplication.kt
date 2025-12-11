package com.sudooom.mahjong.broker

import com.sudooom.mahjong.broker.config.BrokerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("com.sudooom.mahjong")
@EnableConfigurationProperties(BrokerProperties::class)
class BrokerApplication

fun main(args: Array<String>) {
    runApplication<BrokerApplication>(*args)
}
