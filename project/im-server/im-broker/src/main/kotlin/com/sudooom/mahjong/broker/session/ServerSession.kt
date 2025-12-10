package com.sudooom.mahjong.broker.session

import com.sudooom.mahjong.common.annotation.Loggable
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.messaging.Message
import org.springframework.messaging.rsocket.RSocketRequester
import java.time.Instant

/** 服务器会话 用于管理与 Access/Logic 服务的连接 */
data class ServerSession(
        val instanceType: String,
        val instanceId: String,
        val requester: RSocketRequester,
        val connectedAt: Instant = Instant.now(),
        var lastHeartbeat: Instant = Instant.now()
) : Loggable {

    private val _messageFlow = MutableSharedFlow<Message<DataBuffer>>(replay = 0)

    suspend fun sendMessage(message: Message<DataBuffer>) {
        _messageFlow.emit(message)
    }

    fun getMessageFlow(): MutableSharedFlow<Message<DataBuffer>> {
        return _messageFlow
    }

    fun updateHeartbeat() {
        lastHeartbeat = Instant.now()
    }
}
