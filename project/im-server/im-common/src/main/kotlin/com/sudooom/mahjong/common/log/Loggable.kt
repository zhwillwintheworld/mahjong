package com.sudooom.mahjong.common.annotation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 日志接口 实现此接口的类将自动获得一个 logger 属性
 *
 * 使用示例：
 * ```kotlin
 * class MyService : Loggable {
 *     fun doSomething() {
 *         logger.info("Hello")
 *     }
 * }
 * ```
 */
interface Loggable {
    val logger: Logger
        get() = LoggerFactory.getLogger(this.javaClass)
}