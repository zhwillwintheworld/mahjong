package com.sudooom.mahjong.access.service

import com.sudooom.mahjong.access.session.UserSession
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.core.config.BrokerConnectionProperties
import com.sudooom.mahjong.core.holder.BrokerOutboundHolder
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class MessageProcessService(
    private val brokerConnectionProperties: BrokerConnectionProperties,
) : Loggable {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun processUserMessage(message: Flow<Message<ByteBuf>>, session: UserSession) {
        scope.launch {
            message.collect {
                try {
                    val data = it.payload.retain()
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

                    BrokerOutboundHolder.send(
                        MessageBuilder.withPayload(data)
                            .setHeader("routeKey", routeKey)
                            .setHeader("originalInstanceId", originalInstanceId)
                            .setHeader("sessionId", sessionId)
                            .setHeader("messageType", messageType)
                            .build()
                    )

                } finally {
                    it.payload.release()
                }
            }
        }
    }

}