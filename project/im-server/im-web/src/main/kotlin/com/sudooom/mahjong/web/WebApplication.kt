package com.sudooom.mahjong.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/** Web 模块启动类 提供 REST API 服务 */
@SpringBootApplication class WebApplication

fun main(args: Array<String>) {
    runApplication<WebApplication>(*args)
}
