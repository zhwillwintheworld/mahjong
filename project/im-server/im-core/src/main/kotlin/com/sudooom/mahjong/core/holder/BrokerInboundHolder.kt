package com.sudooom.mahjong.core.holder

import com.sudooom.mahjong.common.proto.ServerMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Broker 消息接收 Holder（单例） 用于从 Broker 接收消息，提供 SharedFlow 供外部 consume logic/access 模块通过这个 holder 订阅来自
 * broker 的消息
 */
object BrokerInboundHolder {

    private val inboundFlow =
            MutableSharedFlow<ServerMessage>(replay = 0, extraBufferCapacity = 1024)

    /** 获取只读的 SharedFlow，供外部订阅消费 */
    fun asFlow(): SharedFlow<ServerMessage> = inboundFlow.asSharedFlow()

    /**
     * 内部使用：发布从 Broker 接收到的消息 使用 tryEmit 非挂起方式，适合在 Reactor 回调中调用 由于设置了 extraBufferCapacity =
     * 1024，正常情况下不会失败
     * @return true 如果成功发布，false 如果 buffer 已满
     */
    internal fun publish(message: ServerMessage): Boolean {
        return inboundFlow.tryEmit(message)
    }
}
