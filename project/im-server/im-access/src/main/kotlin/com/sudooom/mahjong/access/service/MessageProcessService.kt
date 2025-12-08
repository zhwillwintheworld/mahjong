package com.sudooom.mahjong.access.service

import com.sudooom.mahjong.access.session.UserSession
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.core.config.BrokerConnectionProperties
import com.sudooom.mahjong.core.holder.BrokerOutboundHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class MessageProcessService(
    private val brokerConnectionProperties: BrokerConnectionProperties,
) : Loggable {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun processUserMessage(message: Flow<Message<DataBuffer>>, session: UserSession) {
        scope.launch {
            message.collect {
                // ⚠️ 重要：因为在异步协程中使用 DataBuffer 并构造新的 payload
                // 需要 retain 增加引用计数，防止原始 Flow 完成后被释放
                val data = DataBufferUtils.retain(it.payload)
                val header = it.headers
                val messageType = header["messageType"] as String?
                val roomId = header["roomId"] as String?
                val sessionId = session.sessionId
                val userId = session.userId
                val originalInstanceId = brokerConnectionProperties.instanceId
                val routeKey = when (messageType) {
                    "USER" -> userId
                    "ROOM" -> roomId
                    "GAME" -> roomId
                    else -> userId
                }

                // 转发给 Broker，之后的 release 由 Broker 层负责
                BrokerOutboundHolder.send(
                    MessageBuilder.withPayload(data)
                        .setHeader("routeKey", routeKey)
                        .setHeader("originalInstanceId", originalInstanceId)
                        .setHeader("sessionId", sessionId)
                        .setHeader("messageType", messageType)
                        .build()
                )
            }
        }
    }
}