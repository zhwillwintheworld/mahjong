package com.sudooom.mahjong.access.service

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.util.JwtUtil
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LoginService(
    private val jwtUtil: JwtUtil
) : Loggable {

    suspend fun login(token: Mono<String>, requester: RSocketRequester) {
        val tokenValue = token.awaitSingleOrNull()
            ?: throw IllegalArgumentException("Token is required")
        
        // 验证 token
        if (!validateToken(tokenValue)) {
            throw IllegalArgumentException("Token is wrong")
        }
        
        // TODO: 处理登录成功后的逻辑
        val userId = jwtUtil.getUserId(tokenValue)
        logger.info("User $userId logged in successfully")
    }
    
    private fun validateToken(token: String): Boolean {
        return jwtUtil.validateToken(token)
    }
}
