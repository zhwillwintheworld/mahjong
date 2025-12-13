package com.sudooom.mahjong.logic.service

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.proto.ClientRequest
import com.sudooom.mahjong.common.constant.MessageHeaders
import com.sudooom.mahjong.core.holder.BrokerInboundHolder
import com.sudooom.mahjong.common.codec.toClientRequest
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

/**
 * 消息分发服务 - Semaphore 并发控制模式
 *
 * 从 Broker 接收消息，解码后根据类型分发到对应的处理器
 * 使用 Semaphore 限制并发数，充分利用 CPU 资源同时避免过载
 */
@Service
class MessageDispatchService(
    private val messageProcessService: MessageProcessService,
) : Loggable {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 并发限制 = CPU 核心数 * 2
    private val concurrencyLimit = Runtime.getRuntime().availableProcessors() * 2
    private val semaphore = Semaphore(concurrencyLimit)

    /**
     * 应用启动后自动开始订阅 BrokerInboundHolder
     * 使用 Semaphore 控制并发数
     */
    @PostConstruct
    fun startDispatching() {
        logger.info("Starting logic message dispatch service with concurrency limit: $concurrencyLimit")

        scope.launch {
            BrokerInboundHolder.asFlow()
                .collect { message ->
                    // 获取许可（如果超过限制会挂起等待）
                    semaphore.acquire()

                    // 每条消息启动独立协程处理
                    launch {
                        try {
                            dispatchMessage(message)
                        } catch (e: Exception) {
                            logger.error("Failed to dispatch message: ${e.message}", e)
                            releaseMessageBuffer(message, "dispatch failed")
                        } finally {
                            // 释放许可
                            semaphore.release()
                        }
                    }
                }
        }

        logger.info("Logic message dispatch service started successfully")
    }

    /**
     * 解码并分发消息到对应的处理器
     */
    private suspend fun dispatchMessage(message: Message<DataBuffer>) {
        val headers = message.headers
        val userId = headers[MessageHeaders.USER_ID] as? String
        val messageType = headers[MessageHeaders.MESSAGE_TYPE] as? String

        if (userId == null || sessionId == null || accessInstanceId == null) {
            logger.warn("Missing required headers (userId, sessionId or accessInstanceId), message discarded")
            releaseMessageBuffer(message, "missing headers")
            return
        }

        try {
            // 使用零拷贝 codec 解码 DataBuffer 为 ClientRequest
            val decodedMessage = message.payload.toClientRequest()

            // 解码完成后释放 DataBuffer
            DataBufferUtils.release(message.payload)

            logger.debug("Decoded ClientRequest: reqId=${decodedMessage.reqId}, type=${decodedMessage.payloadCase}")

            // 根据消息类型分发
            when (decodedMessage.payloadCase) {
                ClientRequest.PayloadCase.CHAT_SEND -> {
                    handleChatMessage(decodedMessage, sessionId, accessInstanceId)
                }

                ClientRequest.PayloadCase.ROOM -> {
                    handleRoomMessage(decodedMessage, sessionId, accessInstanceId)
                }

                ClientRequest.PayloadCase.GAME -> {
                    handleGameMessage(decodedMessage, sessionId, accessInstanceId)
                }

                ClientRequest.PayloadCase.HEARTBEAT -> {
                    handleHeartbeat(decodedMessage, sessionId, accessInstanceId)
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
