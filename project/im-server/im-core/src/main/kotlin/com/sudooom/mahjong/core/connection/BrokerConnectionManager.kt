package com.sudooom.mahjong.core.connection

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.core.config.BrokerConnectionProperties
import com.sudooom.mahjong.core.holder.BrokerInboundHolder
import com.sudooom.mahjong.core.holder.BrokerOutboundHolder
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.messaging.Message
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Broker 连接管理器 负责管理与 Broker 的 RSocket 连接和 request-channel 通信
 *
 * 连接重连逻辑在 RSocketConnector 中配置 Channel 重建逻辑通过 retryWhen 实现
 */
@Component
class BrokerConnectionManager(
    private val brokerRSocketRequesterBuilder: RSocketRequester.Builder,
    private val properties: BrokerConnectionProperties,
) : Loggable {

    private val connected = AtomicBoolean(false)
    private val requesterRef = AtomicReference<RSocketRequester?>()
    private val channelDisposableRef = AtomicReference<Disposable?>()
    val typeRef = object : ParameterizedTypeReference<Message<DataBuffer>>() {}

    /** 应用启动时自动连接 Broker 并建立 request-channel */
    @PostConstruct
    fun connect() {
        logger.info("Connecting to Broker at ${properties.host}:${properties.port}...")

        // 在 PostConstruct 中创建 RSocketRequester，建立实际连接
        val requester = brokerRSocketRequesterBuilder.tcp(properties.host, properties.port)

        requesterRef.set(requester)

        // 直接建立 request-channel，在 channel 上处理重试逻辑
        establishChannel(requester)
    }

    /** 建立 request-channel 连接 使用 retryWhen 在 channel 断开时自动重建 */
    private fun establishChannel(requester: RSocketRequester) {
        logger.info("Establishing request-channel to ${properties.channelRoute}...")

        val channelDisposable =
            requester
                .route(properties.channelRoute)
                .data(BrokerOutboundHolder.getMessageFlow())
                .retrieveFlux(typeRef)
                // ⚠️ 关键：处理流中被丢弃的 Message<DataBuffer>（异常、取消、背压等情况）
                // 注意：流的类型是 Message<DataBuffer>，所以要匹配 Message 类型
                .doOnDiscard(Message::class.java) { message ->
                    val payload = message.payload
                    if (payload is DataBuffer) {
                        logger.debug("Releasing discarded DataBuffer from Message")
                        DataBufferUtils.release(payload)
                    }
                }
                .doOnSubscribe {
                    connected.set(true)
                    logger.info("Request-channel established successfully")
                }
                .doOnNext { inbound ->
                    // 将接收到的消息发布到 inboundHolder
                    if (!BrokerInboundHolder.publish(inbound)) {
                        logger.warn("Failed to publish message, buffer full")
                        // ⚠️ 如果 buffer 满了，消息被丢弃，需要释放 DataBuffer
                        DataBufferUtils.release(inbound.payload)
                    }
                }
                .doOnError { error ->
                    connected.set(false)
                    logger.error("Request-channel error: ${error.message}", error)
                }
                .doOnComplete {
                    connected.set(false)
                    logger.warn("Request-channel completed")
                }
                .retryWhen(
                    Retry.backoff(
                        if (properties.maxReconnectAttempts < 0)
                            Long.MAX_VALUE
                        else properties.maxReconnectAttempts.toLong(),
                        Duration.ofMillis(properties.reconnectIntervalMs)
                    )
                        .maxBackoff(
                            Duration.ofMillis(properties.maxReconnectIntervalMs)
                        )
                        .doBeforeRetry { signal ->
                            logger.info(
                                "Retrying request-channel, attempt ${signal.totalRetries() + 1}..."
                            )
                        }
                )
                .subscribe()

        channelDisposableRef.set(channelDisposable)
    }

    /** 获取当前连接状态 */
    fun isConnected(): Boolean = connected.get()

    /** 获取 RSocketRequester（可能为 null） */
    fun getRequester(): RSocketRequester? = requesterRef.get()

    /** 应用关闭时断开连接 */
    @PreDestroy
    fun disconnect() {
        logger.info("Disconnecting from Broker...")
        channelDisposableRef.get()?.dispose()
        requesterRef.get()?.rsocketClient()?.dispose()
        connected.set(false)
    }
}
