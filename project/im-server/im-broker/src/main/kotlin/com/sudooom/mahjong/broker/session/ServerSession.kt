package com.sudooom.mahjong.broker.session

import com.sudooom.mahjong.common.annotation.Loggable
import java.time.Instant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.messaging.rsocket.RSocketRequester

/** 服务器会话 用于管理与 Access/Logic 服务的连接 */
data class ServerSession(
        val sessionId: String,
        val instanceType: String,
        val instanceId: String,
        val requester: RSocketRequester,
        val connectedAt: Instant = Instant.now(),
        var lastHeartbeat: Instant = Instant.now()
) : Loggable {

    private val _messageFlow = MutableSharedFlow<ByteArray>(replay = 0)
    val messageFlow: SharedFlow<ByteArray> = _messageFlow.asSharedFlow()

    suspend fun sendMessage(message: ByteArray) {
        _messageFlow.emit(message)
    }

    fun getMessageFlow(): MutableSharedFlow<ByteArray> {
        return _messageFlow
    }

    fun updateHeartbeat() {
        lastHeartbeat = Instant.now()
    }
}
