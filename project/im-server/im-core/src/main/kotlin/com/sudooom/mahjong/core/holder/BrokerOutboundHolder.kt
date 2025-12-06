package com.sudooom.mahjong.core.holder

import com.sudooom.mahjong.common.proto.ServerMessage
import com.sudooom.mahjong.common.route.RoutedMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.asFlux
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux

/** Broker 消息发送 Holder（单例） 用于向 Broker 发送消息，不关注连接状态 logic/access 模块通过这个 holder 向 broker 发送消息 */
object BrokerOutboundHolder {

    private val outboundFlow = MutableSharedFlow<RoutedMessage>(replay = 0, extraBufferCapacity = 1024)

    /** 获取内部的 MutableSharedFlow，供 BrokerConnectionManager 订阅 */
    fun getMessageFlow(): Flux<Message<ServerMessage>> = outboundFlow.map {
        MessageBuilder.withPayload(it.payload)
            .setHeader("route_type", it.routeMetadata.type.code)
            .setHeader("route_key", it.routeMetadata.routeKey)
            .build()
    }.asFlux()

    /** 挂起发送消息到 Broker */
    suspend fun send(message: RoutedMessage) {
        outboundFlow.emit(message)
    }
}
