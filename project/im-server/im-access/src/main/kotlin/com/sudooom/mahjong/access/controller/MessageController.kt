package com.sudooom.mahjong.access.controller

import com.sudooom.mahjong.access.service.LoginService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

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
    @MessageMapping("connect")
    suspend fun connect(
        @Payload userId: Mono<String>,
        requester: RSocketRequester
    ){
       loginService.login(userId, requester)
    }
    

}
