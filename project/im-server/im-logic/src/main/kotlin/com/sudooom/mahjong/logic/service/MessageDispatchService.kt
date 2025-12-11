package com.sudooom.mahjong.logic.service

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.proto.ClientRequest
import com.sudooom.mahjong.core.holder.BrokerInboundHolder
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.protobuf.ProtobufDecoder
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

/**
 * 消息分发服务
 * 从 Broker 接收消息并解码后根据类型分发到对应的处理器
 */
@Service
class MessageDispatchService(
    private val messageProcessService: MessageProcessService,
) : Loggable {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val protobufDecoder = ProtobufDecoder()

    /**
     * 应用启动后自动开始订阅 BrokerInboundHolder
     */
    @PostConstruct
    fun startDispatching() {
        logger.info("Starting message dispatch service...")

        scope.launch {
            BrokerInboundHolder.asFlow()
                .collect { message ->
                    try {
                        dispatchMessage(message)
                    } catch (e: Exception) {
                        logger.error("Failed to dispatch message: ${e.message}", e)
                        // 如果分发失败，需要释放 DataBuffer
                        releaseMessageBuffer(message, "dispatch failed")
                    }
                }
        }

        logger.info("Message dispatch service started successfully")
    }

    /**
     * 解码并分发消息到对应的处理器
     */
    private suspend fun dispatchMessage(message: Message<DataBuffer>) {
        val headers = message.headers
        val sessionId = headers["sessionId"] as? String
        val originalInstanceId = headers["originalInstanceId"] as? String
        val messageType = headers["messageType"] as? String

        if (sessionId == null || originalInstanceId == null) {
            logger.warn("Missing required headers (sessionId or originalInstanceId), message discarded")
            releaseMessageBuffer(message, "missing headers")
            return
        }

        try {
            // 使用 ProtobufDecoder 解码 DataBuffer 为 ClientRequest
            // ⚠️ 重要：需要 retain 增加引用计数，因为在异步处理中使用
            val dataBuffer = DataBufferUtils.retain(message.payload)

            val decodedMessage = protobufDecoder.decode(
                dataBuffer,
                ResolvableType.forClass(ClientRequest::class.java),
                null,
                null
            ) as? ClientRequest

            // 解码完成后释放 DataBuffer
            DataBufferUtils.release(dataBuffer)

            if (decodedMessage == null) {
                logger.warn("Failed to decode message as ClientRequest")
                return
            }

            logger.debug("Decoded ClientRequest: reqId=${decodedMessage.reqId}, type=${decodedMessage.payloadCase}")

            // 根据消息类型分发
            when (decodedMessage.payloadCase) {
                ClientRequest.PayloadCase.CHAT_SEND -> {
                    handleChatMessage(decodedMessage, sessionId, originalInstanceId)
                }

                ClientRequest.PayloadCase.ROOM -> {
                    handleRoomMessage(decodedMessage, sessionId, originalInstanceId)
                }

                ClientRequest.PayloadCase.GAME -> {
                    handleGameMessage(decodedMessage, sessionId, originalInstanceId)
                }

                ClientRequest.PayloadCase.HEARTBEAT -> {
                    handleHeartbeat(decodedMessage, sessionId, originalInstanceId)
                }

                else -> {
                    logger.warn("Unknown message type: ${decodedMessage.payloadCase}")
                }
            }

        } catch (e: Exception) {
            logger.error("Error decoding or processing message: ${e.message}", e)
            releaseMessageBuffer(message, "decode error")
        }
    }

    /**
     * 处理聊天消息
     */
    private suspend fun handleChatMessage(
        request: ClientRequest,
        sessionId: String,
        accessInstanceId: String
    ) {
        logger.info("Handling chat message: reqId=${request.reqId}, from session=$sessionId")

        val chatSendReq = request.chatSend
        // TODO: 实现聊天消息的业务逻辑（存储到数据库等）

        // 发送确认响应
        messageProcessService.sendChatAck(
            reqId = request.reqId,
            sessionId = sessionId,
            accessInstanceId = accessInstanceId
        )

        // TODO: 如果是群聊或房间聊天，需要推送到其他用户
    }

    /**
     * 处理房间消息
     */
    private suspend fun handleRoomMessage(
        request: ClientRequest,
        sessionId: String,
        accessInstanceId: String
    ) {
        logger.info("Handling room message: reqId=${request.reqId}, action=${request.room.action}")

        val roomReq = request.room
        // TODO: 实现房间相关的业务逻辑（创建、加入、离开、准备等）

        // 发送响应
        messageProcessService.sendRoomResponse(
            reqId = request.reqId,
            sessionId = sessionId,
            accessInstanceId = accessInstanceId,
            roomId = roomReq.roomId
        )
    }

    /**
     * 处理游戏消息
     */
    private suspend fun handleGameMessage(
        request: ClientRequest,
        sessionId: String,
        accessInstanceId: String
    ) {
        logger.info("Handling game message: reqId=${request.reqId}, roomId=${request.game.roomId}")

        val gameReq = request.game
        // TODO: 实现游戏相关的业务逻辑（麻将操作等）

        // 游戏消息通常不需要立即响应，而是通过推送通知所有玩家
    }

    /**
     * 处理心跳
     */
    private suspend fun handleHeartbeat(
        request: ClientRequest,
        sessionId: String,
        accessInstanceId: String
    ) {
        logger.debug("Handling heartbeat: reqId=${request.reqId}")

        messageProcessService.sendHeartbeatResponse(
            reqId = request.reqId,
            sessionId = sessionId,
            accessInstanceId = accessInstanceId
        )
    }

    /**
     * 释放消息中的 DataBuffer
     * 统一处理消息无法分发时的资源释放
     */
    private fun releaseMessageBuffer(message: Message<DataBuffer>, reason: String) {
        logger.debug("Releasing message buffer, reason: $reason")
        DataBufferUtils.release(message.payload)
    }

    /**
     * 应用关闭时取消协程
     */
    @PreDestroy
    fun stopDispatching() {
        logger.info("Stopping message dispatch service...")
        scope.cancel()
        logger.info("Message dispatch service stopped")
    }
}
