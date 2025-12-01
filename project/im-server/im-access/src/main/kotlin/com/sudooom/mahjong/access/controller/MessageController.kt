package com.sudooom.mahjong.access.controller

import com.sudooom.mahjong.access.session.SessionManager
import com.sudooom.mahjong.access.service.MessagePushService
import com.sudooom.mahjong.common.annotation.Loggable
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import java.nio.ByteBuffer

/**
 * 消息控制器
 * 处理 RSocket 消息请求
 */
@Controller("api")
class MessageController{
    

    /**
     * 建立连接
     */
    @MessageMapping("connect")
    suspend fun connect(
        @Payload userId: Mono<String>,
        requester: RSocketRequester
    ){
        logger.info("用户连接: $userId")
        
        return Mono.fromCallable {
            // 创建会话
            val session = sessionManager.createSession(userId, requester)
            
            // 监听连接断开
            requester.rsocket()?.onClose()?.subscribe {
                logger.info("用户断开连接: $userId")
                sessionManager.removeSession(userId)
            }
            
            "Connected: ${session.sessionId}"
        }
    }
    

}
