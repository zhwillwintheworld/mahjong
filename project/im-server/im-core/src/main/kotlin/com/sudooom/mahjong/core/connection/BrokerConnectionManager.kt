package com.sudooom.mahjong.core.connection

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.route.MessageFrameCodec
import com.sudooom.mahjong.common.route.RoutedMessage
import com.sudooom.mahjong.core.config.BrokerConnectionProperties
import com.sudooom.mahjong.core.holder.BrokerInboundHolder
import com.sudooom.mahjong.core.holder.BrokerOutboundHolder
import io.netty.buffer.ByteBuf
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asPublisher
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux

/**
 * Broker 连接管理器 负责管理与 Broker 的 RSocket 连接和 request-channel 通信
 *
 * 使用 MessageFrameCodec 将路由信息和消息内容打包成单个 ByteBuf： [metadataLength: 2 bytes][metadata: N
 * bytes][payload: M bytes]
 */
@Component
class BrokerConnectionManager(
        private val brokerRSocketRequester: RSocketRequester,
        private val properties: BrokerConnectionProperties,
) : Loggable {

    private val connected = AtomicBoolean(false)
    private val disposableRef = AtomicReference<Disposable?>()
    private val channelDisposableRef = AtomicReference<Disposable?>()

    /** 应用启动时自动连接 Broker 并建立 request-channel */
    @PostConstruct
    fun connect() {
        logger.info("Connecting to Broker at ${properties.host}:${properties.port}...")

        val disposable =
                brokerRSocketRequester
                        .rsocketClient()
                        .source()
                        .doOnNext { rSocket ->
                            connected.set(true)
                            logger.info(
                                    "Successfully connected to Broker at ${properties.host}:${properties.port}"
                            )

                            // 建立 request-channel 连接
                            establishChannel()

                            // 监听连接关闭事件
                            rSocket.onClose()
                                    .doOnTerminate {
                                        connected.set(false)
                                        logger.warn("Connection to Broker closed")
                                        channelDisposableRef.get()?.dispose()
                                    }
                                    .subscribe()
                        }
                        .doOnError { error ->
                            connected.set(false)
                            logger.error("Failed to connect to Broker: ${error.message}", error)
                        }
                        .subscribe()

        disposableRef.set(disposable)
    }

    /** 建立 request-channel 连接 */
    private fun establishChannel() {
        logger.info("Establishing request-channel to ${properties.channelRoute}...")

        // 将 RoutedMessage Flow 转换为打包的 ByteBuf Flux
        val outboundFrames: Flux<ByteBuf> =
                Flux.from(
                        BrokerOutboundHolder.getFlow()
                                .map { routedMessage -> encodeFrame(routedMessage) }
                                .asPublisher()
                )

        val channelDisposable =
                brokerRSocketRequester
                        .route(properties.channelRoute)
                        .data(outboundFrames)
                        .retrieveFlux(ByteBuf::class.java)
                        .doOnNext { inbound ->
                            // 将接收到的消息发布到 inboundHolder
                            BrokerInboundHolder.emit(inbound)
                        }
                        .doOnError { error ->
                            logger.error("Request-channel error: ${error.message}", error)
                        }
                        .doOnComplete { logger.warn("Request-channel completed") }
                        .subscribe()

        channelDisposableRef.set(channelDisposable)
        logger.info("Request-channel established successfully")
    }

    /**
     * 将 RoutedMessage 打包为 ByteBuf 格式: [metadataLength: 2 bytes][metadata: N bytes][payload: M
     * bytes]
     */
    private fun encodeFrame(message: RoutedMessage): ByteBuf {
        return MessageFrameCodec.encode(message)
    }

    /** 应用关闭时断开连接 */
    @PreDestroy
    fun disconnect() {
        logger.info("Disconnecting from Broker...")
        channelDisposableRef.get()?.dispose()
        disposableRef.get()?.dispose()
        connected.set(false)
    }
}
