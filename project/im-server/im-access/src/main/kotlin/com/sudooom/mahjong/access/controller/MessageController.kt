package com.sudooom.mahjong.access.controller

import com.sudooom.mahjong.access.service.ConnectService
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.flow.Flow
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

/** 消息控制器 处理 RSocket 消息请求 */
@Controller("api")
class MessageController(
        private val connectService: ConnectService,
) {

    /** 建立连接 */
    @ConnectMapping("connect")
    suspend fun connect(@Payload token: Mono<String>, requester: RSocketRequester) {
        connectService.login(token, requester)
    }

    @MessageMapping("im")
    suspend fun im(@Payload messages: Flow<Message<ByteBuf>>, requester: RSocketRequester): Flow<Message<ByteBuf>> {
        return connectService.message(messages, requester)
    }
}
