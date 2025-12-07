package com.sudooom.mahjong.access.session

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.messaging.Message
import org.springframework.messaging.rsocket.RSocketRequester
import java.time.Instant

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
    private val _messageFlow = MutableSharedFlow<Message<ByteBuf>>(replay = 0)
    val messageFlow: SharedFlow<Message<ByteBuf>> = _messageFlow.asSharedFlow()

    suspend fun sendMessage(message: Message<ByteBuf>) {
        _messageFlow.emit(message)
    }

    fun getMessageFlow(): MutableSharedFlow<Message<ByteBuf>> {
        return _messageFlow
    }

    fun updateHeartbeat() {
        lastHeartbeat = Instant.now()
    }
}
