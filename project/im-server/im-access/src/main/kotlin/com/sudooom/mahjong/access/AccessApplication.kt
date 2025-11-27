package com.sudooom.mahjong.access

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Access 模块启动类
 * 有状态服务，负责维护用户长连接
 */
@SpringBootApplication
class AccessApplication

fun main(args: Array<String>) {
    runApplication<AccessApplication>(*args)
}
