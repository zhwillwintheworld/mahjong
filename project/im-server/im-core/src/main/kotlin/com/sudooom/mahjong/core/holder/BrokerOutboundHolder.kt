package com.sudooom.mahjong.core.holder

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.flow.MutableSharedFlow

/** Broker 消息发送 Holder（单例） 用于向 Broker 发送消息，不关注连接状态 logic/access 模块通过这个 holder 向 broker 发送消息 */
object BrokerOutboundHolder {

    private val outboundFlow = MutableSharedFlow<ByteBuf>(replay = 0, extraBufferCapacity = 1024)

    /** 获取内部的 MutableSharedFlow，供 BrokerConnectionManager 订阅 */
    fun getFlow(): MutableSharedFlow<ByteBuf> = outboundFlow

    /** 发送消息到 Broker */
    fun emit(message: ByteBuf): Boolean {
        return outboundFlow.tryEmit(message)
    }

    /** 挂起发送消息到 Broker */
    suspend fun send(message: ByteBuf) {
        outboundFlow.emit(message)
    }
}
