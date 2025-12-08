package com.sudooom.mahjong.access.service

import com.sudooom.mahjong.access.session.SessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.core.holder.BrokerInboundHolder
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

/**
 * 消息分发服务
 * 从 Broker 接收消息并根据 metadata 路由到对应的用户会话
 */
@Service
class MessageDispatchService(
    private val sessionManager: SessionManager,
) : Loggable {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
     * 根据消息的 metadata 分发到对应的用户
     */
    private suspend fun dispatchMessage(message: Message<DataBuffer>) {
        val headers = message.headers
        val sessionId = headers["sessionId"] as? String
        when {
            sessionId != null -> {
                // 根据 sessionId 路由到特定会话
                val session = sessionManager.getSession(sessionId)
                if (session != null) {
                    session.sendMessage(message)
                    logger.debug("Message dispatched to session: $sessionId")
                } else {
                    logger.warn("Session not found: $sessionId, message discarded")
                    releaseMessageBuffer(message, "session not found")
                }
            }

            else -> {
                logger.warn("Unable to route message: no sessionId in metadata")
                releaseMessageBuffer(message, "no route info")
            }
        }
    }

    /**
     * 释放消息中的 DataBuffer
     * 统一处理消息无法分发时的资源释放
     */
    private fun releaseMessageBuffer(message: Message<DataBuffer>, reason: String) {
        logger.debug("Releasing message buffer, reason: $reason")
        // todo 回传消息到 logic 告诉他消息发送失败
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
