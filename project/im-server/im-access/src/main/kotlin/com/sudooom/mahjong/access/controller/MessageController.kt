package com.sudooom.mahjong.access.controller

import com.sudooom.mahjong.access.service.LoginService
import com.sudooom.mahjong.access.session.SessionManager
import com.sudooom.mahjong.access.service.MessagePushService
import com.sudooom.mahjong.common.annotation.Loggable
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import java.nio.ByteBuffer

/**
 * 消息控制器
 * 处理 RSocket 消息请求
 */
@Controller("api")
class MessageController(
    private val loginService: LoginService
){
    

    /**
     * 建立连接
     */
    @ConnectMapping("connect")
    suspend fun connect(
        @Payload token: Mono<String>,
        requester: RSocketRequester
    ){
        loginService.login(token, requester)
    }
    

}
