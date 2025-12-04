package com.sudooom.mahjong.core.holder

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Broker 消息接收 Holder（单例） 用于从 Broker 接收消息，提供 SharedFlow 供外部 consume logic/access 模块通过这个 holder 订阅来自
 * broker 的消息
 */
object BrokerInboundHolder {

    private val inboundFlow = MutableSharedFlow<ByteBuf>(replay = 0, extraBufferCapacity = 1024)

    /** 获取只读的 SharedFlow，供外部订阅消费 */
    fun asFlow(): SharedFlow<ByteBuf> = inboundFlow.asSharedFlow()

    /** 内部使用：发布从 Broker 接收到的消息 */
    internal fun emit(message: ByteBuf): Boolean {
        return inboundFlow.tryEmit(message)
    }

    /** 内部使用：挂起发布从 Broker 接收到的消息 */
    internal suspend fun publish(message: ByteBuf) {
        inboundFlow.emit(message)
    }
}
