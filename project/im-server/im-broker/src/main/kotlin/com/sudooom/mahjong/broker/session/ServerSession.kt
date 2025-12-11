package com.sudooom.mahjong.broker.session

import com.sudooom.mahjong.common.annotation.Loggable
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.messaging.Message
import org.springframework.messaging.rsocket.RSocketRequester
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

/** 服务器会话 用于管理与 Access/Logic 服务的连接 */
data class ServerSession(
    val instanceType: String,
    val instanceId: String,
    val requester: RSocketRequester,
    val connectedAt: Instant = Instant.now(),
    var lastHeartbeat: Instant = Instant.now()
) : Loggable {

    /** 服务状态，默认为 ONLINE */
    private val _status = AtomicReference(ServiceStatus.ONLINE)

    private val _messageFlow = MutableSharedFlow<Message<DataBuffer>>(replay = 0)

    /** 获取当前服务状态 */
    val status: ServiceStatus
        get() = _status.get()

    suspend fun sendMessage(message: Message<DataBuffer>) {
        _messageFlow.emit(message)
    }

    fun getMessageFlow(): MutableSharedFlow<Message<DataBuffer>> {
        return _messageFlow
    }

    fun updateHeartbeat() {
        lastHeartbeat = Instant.now()
    }

    /**
     * 标记服务为离线
     * 连接断开时调用
     */
    fun markOffline() {
        val old = _status.getAndSet(ServiceStatus.OFFLINE)
        if (old != ServiceStatus.OFFLINE) {
            logger.info("服务离线: $instanceId: $old -> OFFLINE")
        }
    }

    /**
     * 判断是否可以接收新请求（用于路由选择）
     */
    fun canAcceptNewRequests(): Boolean = _status.get().canAcceptNewRequests()
}
