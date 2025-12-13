package com.sudooom.mahjong.core.holder

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.withTimeout
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.messaging.Message
import reactor.core.publisher.Flux

/**
 * Broker 消息发送 Holder（单例）
 *
 * Access/Logic 模块通过这个 holder 向 Broker 发送消息
 * 背压策略：超时后丢弃消息并释放 DataBuffer
 */
object BrokerOutboundHolder {

    private val outboundFlow = MutableSharedFlow<Message<DataBuffer>>(replay = 0, extraBufferCapacity = 1024)

    // 发送超时时间（毫秒）
    private const val SEND_TIMEOUT_MS = 1000L

    /** 获取内部的 MutableSharedFlow，供 BrokerConnectionManager 订阅 */
    fun getMessageFlow(): Flux<Message<DataBuffer>> = outboundFlow.asFlux()

    /**
     * 发送消息到 Broker（带超时背压）
     *
     * 背压处理：如果超时，释放 DataBuffer 并返回 false
     *
     * @return true 成功发送, false 超时丢弃
     */
    suspend fun send(message: Message<DataBuffer>): Boolean {
        return try {
            withTimeout(SEND_TIMEOUT_MS) {
                outboundFlow.emit(message)
                true
            }
        } catch (e: TimeoutCancellationException) {
            // ⚠️ 背压处理：超时释放 DataBuffer 防止内存泄漏
            DataBufferUtils.release(message.payload)
            false
        }
    }
}

