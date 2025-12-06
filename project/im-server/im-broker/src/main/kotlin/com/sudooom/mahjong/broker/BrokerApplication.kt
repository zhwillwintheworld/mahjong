package com.sudooom.mahjong.broker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("com.sudooom.mahjong")
class BrokerApplication

fun main(args: Array<String>) {
    runApplication<BrokerApplication>(*args)
}
