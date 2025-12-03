package com.sudooom.mahjong.access

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Access 模块启动类
 * 有状态服务，负责维护用户长连接
 */
@SpringBootApplication
@ComponentScan("com.sudooom.mahjong.access", "com.sudooom.mahjong.common")
class AccessApplication

fun main(args: Array<String>) {
    runApplication<AccessApplication>(*args)
}
