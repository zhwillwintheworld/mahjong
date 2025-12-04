package com.sudooom.mahjong.access.service

import com.sudooom.mahjong.access.session.SessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.proto.ClientRequest
import com.sudooom.mahjong.common.proto.ClientResponse
import com.sudooom.mahjong.common.util.JwtUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ConnectService(private val jwtUtil: JwtUtil, private val sessionManager: SessionManager) :
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
        logger.info("User $userId  SessionId ${session.sessionId}logged in successfully")
    }

    suspend fun message(
            messages: Flow<ClientRequest>,
            requester: RSocketRequester,
    ): Flow<ClientResponse> {
        val session =
                sessionManager.getSession(requester)
                        ?: throw IllegalArgumentException("User not found")
        messages.map {}

        return session.getMessageFlow()
    }
}
