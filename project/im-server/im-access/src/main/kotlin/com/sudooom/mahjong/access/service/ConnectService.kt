package com.sudooom.mahjong.access.service

import com.sudooom.mahjong.access.session.SessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.util.JwtUtil
import com.sudooom.mahjong.core.config.BrokerConnectionProperties
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.messaging.Message
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ConnectService(
    private val jwtUtil: JwtUtil,
    private val sessionManager: SessionManager,
    private val brokerConnectionProperties: BrokerConnectionProperties,
    private val messageProcessService: MessageProcessService,

    ) :
    Loggable {

    suspend fun login(token: Mono<String>, requester: RSocketRequester) {
        val tokenValue =
            token.awaitSingleOrNull() ?: throw IllegalArgumentException("Token is required")
        val claims = jwtUtil.parseToken(tokenValue)
        val userId = jwtUtil.getClaim(claims, JwtUtil.CLAIM_USER_ID, String::class.java)
        val deviceId = jwtUtil.getClaim(claims, JwtUtil.CLAIM_DEVICE_ID, String::class.java)
        val platform = jwtUtil.getClaim(claims, JwtUtil.CLAIM_PLATFORM, String::class.java)
        val session =
            sessionManager.createSession(
                userId = userId,
                deviceId = deviceId,
                platform = platform,
                requester = requester,
            )
        logger.info("User $userId SessionId ${session.sessionId} logged in successfully")

        // 监听连接关闭事件
        requester
            .rsocket()
            ?.onClose()
            ?.doOnTerminate {
                logger.info("User $userId SessionId ${session.sessionId} disconnected")
                sessionManager.removeSession(requester)
            }
            ?.subscribe()
    }

    suspend fun message(
        messages: Flow<Message<DataBuffer>>,
        requester: RSocketRequester,
    ): Flow<Message<DataBuffer>> {
        val session =
            sessionManager.getSession(requester)
                ?: throw IllegalArgumentException("User not found")
        messageProcessService.processUserMessage(messages, session)
        return session.getMessageFlow()
    }
}
