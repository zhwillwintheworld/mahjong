package com.sudooom.mahjong.access.session

import com.sudooom.mahjong.common.proto.ClientResponse
import java.time.Instant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.messaging.rsocket.RSocketRequester

/** 用户会话 */
data class UserSession(
        val sessionId: String,
        val deviceId: String,
        val userId: String,
        val platform: String,
        val requester: RSocketRequester,
        val connectedAt: Instant = Instant.now(),
        var lastHeartbeat: Instant = Instant.now()
) {
    private val _messageFlow = MutableSharedFlow<ClientResponse>(replay = 0)
    val messageFlow: SharedFlow<ClientResponse> = _messageFlow.asSharedFlow()

    suspend fun sendMessage(message: ClientResponse) {
        _messageFlow.emit(message)
    }

    fun getMessageFlow(): MutableSharedFlow<ClientResponse> {
        return _messageFlow
    }

    fun updateHeartbeat() {
        lastHeartbeat = Instant.now()
    }
}
