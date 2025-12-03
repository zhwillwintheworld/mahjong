package com.sudooom.mahjong.access.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.messaging.rsocket.RSocketRequester
import java.time.Instant

/**
 * 用户会话
 */
data class UserSession(
    val sessionId: String,
    val deviceId: String,
    val userId: String,
    val requester: RSocketRequester,
    val connectedAt: Instant = Instant.now(),
    var lastHeartbeat: Instant = Instant.now()
) {
    private val _messageFlow = MutableSharedFlow<ByteArray>(replay = 0)
    val messageFlow: SharedFlow<ByteArray> = _messageFlow.asSharedFlow()
    
    suspend fun sendMessage(message: ByteArray) {
        _messageFlow.emit(message)
    }
    
    fun updateHeartbeat() {
        lastHeartbeat = Instant.now()
    }
}
