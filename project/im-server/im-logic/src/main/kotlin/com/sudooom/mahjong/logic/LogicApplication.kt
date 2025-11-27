package com.sudooom.mahjong.logic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Logic 模块启动类
 * 无状态服务，负责业务逻辑处理
 */
@SpringBootApplication
class LogicApplication

fun main(args: Array<String>) {
    runApplication<LogicApplication>(*args)
}
