package com.sudooom.mahjong.broker.controller

import com.sudooom.mahjong.broker.service.BrokerConnectService
import kotlinx.coroutines.flow.Flow
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

/** Broker 控制器 处理来自 Access/Logic 服务的 RSocket 请求 */
@Controller("broker")
class BrokerController(private val brokerConnectService: BrokerConnectService) {

    /**
     * 处理连接请求 路由: broker.connect
     * @param token JWT token（包含加密的 instanceType 和 instanceId）
     * @param requester RSocket 请求器
     */
    @ConnectMapping("connect")
    suspend fun connect(@Payload token: Mono<String>, requester: RSocketRequester) {
        brokerConnectService.connect(token, requester)
    }

    /**
     * 处理消息通道 路由: broker.channel
     * @param messages 输入消息流
     * @param requester RSocket 请求器
     * @return 输出消息流
     */
    @MessageMapping("channel")
    suspend fun channel(
            @Payload messages: Flow<ByteArray>,
            requester: RSocketRequester
    ): Flow<ByteArray> {
        return brokerConnectService.channel(messages, requester)
    }
}
