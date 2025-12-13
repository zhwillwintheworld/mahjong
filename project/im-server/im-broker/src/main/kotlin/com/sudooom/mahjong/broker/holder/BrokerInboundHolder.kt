package com.sudooom.mahjong.broker.holder

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.messaging.Message

/**
 * Broker 入站消息 Holder（单例）
 *
 * 汇聚来自所有 Access/Logic 连接的消息到统一的 SharedFlow
 * 消息处理器订阅此 Flow 统一处理，不关心消息来自哪个具体连接
 *
 * 背压策略：buffer 满时丢弃消息并释放 DataBuffer
 */
object BrokerInboundHolder {

    private val inboundFlow =
        MutableSharedFlow<Message<DataBuffer>>(replay = 0, extraBufferCapacity = 4096)

    /**
     * 获取只读的 SharedFlow，供 MessageDispatchService 订阅消费
     */
    fun asFlow(): SharedFlow<Message<DataBuffer>> = inboundFlow.asSharedFlow()

    /**
     * 发布消息到入站流
     *
     * 背压处理：如果 buffer 已满，会释放 DataBuffer 并返回 false
     *
     * @return true 成功发布, false buffer 满导致丢弃
     */
    fun publish(message: Message<DataBuffer>): Boolean {
        val success = inboundFlow.tryEmit(message)
        if (!success) {
            // ⚠️ 背压处理：释放 DataBuffer 防止内存泄漏
            DataBufferUtils.release(message.payload)
        }
        return success
    }
}

