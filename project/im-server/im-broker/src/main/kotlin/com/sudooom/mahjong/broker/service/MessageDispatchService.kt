package com.sudooom.mahjong.broker.service

import com.sudooom.mahjong.broker.router.MessageRouter
import com.sudooom.mahjong.broker.session.ServerSession
import com.sudooom.mahjong.broker.session.ServerSessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.route.RouteMetadata
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

/**
 * 消息分发服务
 * 处理来自 Access 的消息，根据路由规则分发到对应的 Logic 实例
 * 处理来自 Logic 的消息，根据 accessInstanceId 分发到对应的 Access 实例
 */
@Service
class MessageDispatchService(
    private val sessionManager: ServerSessionManager,
    private val messageRouter: MessageRouter,
) : Loggable {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 订阅消息流并进行分发
     * @param messages 输入消息流
     * @param sourceSession 消息来源会话
     */
    fun subscribeAndDispatch(messages: Flow<Message<DataBuffer>>, sourceSession: ServerSession) {
        scope.launch {
            messages.collect { message ->
                try {
                    dispatchMessage(message, sourceSession)
                } catch (e: Exception) {
                    logger.error("Failed to dispatch message: ${e.message}", e)
                    releaseMessageBuffer(message, "dispatch failed")
                }
            }
        }
    }

    /**
     * 根据消息来源和 metadata 分发消息
     * - 来自 Access 的消息：路由到 Logic
     * - 来自 Logic 的消息：路由到 Access
     */
    private suspend fun dispatchMessage(message: Message<DataBuffer>, sourceSession: ServerSession) {

        when (sourceSession.instanceType) {
            "ACCESS" -> {
                // 来自 Access 的消息，路由到 Logic
                dispatchToLogic(message)
            }

            "LOGIC" -> {
                // 来自 Logic 的消息，路由到 Access
                dispatchToAccess(message)
            }

            else -> {
                logger.warn("Unknown instance type: ${sourceSession.instanceType}, message discarded")
                releaseMessageBuffer(message, "unknown instance type")
            }
        }
    }

    /**
     * 将消息路由到 Logic 实例
     * 使用一致性哈希进行路由选择
     */
    private suspend fun dispatchToLogic(message: Message<DataBuffer>) {
        val headers = message.headers

        // 从 headers 中提取路由信息
        val routeType = headers["routeType"] as? String
        val routeKey = headers["routeKey"] as? String

        if (routeType == null || routeKey == null) {
            logger.warn("Missing route metadata, message discarded")
            releaseMessageBuffer(message, "missing route metadata")
            return
        }

        val metadata = RouteMetadata.fromTypeString(routeType, routeKey)
        val targetSession = messageRouter.route(metadata)

        if (targetSession == null) {
            logger.warn("No Logic instances available for routing")
            releaseMessageBuffer(message, "no Logic instances")
            return
        }

        // 发送到目标 Logic 实例
        targetSession.let { session ->
            try {
                session.sendMessage(message)
                logger.debug("Message dispatched to Logic: ${session.instanceId}")
            } catch (e: Exception) {
                logger.error("Failed to send message to Logic ${session.instanceId}: ${e.message}", e)
            }
        }
    }

    /**
     * 将消息路由到 Access 实例
     * 根据 accessInstanceId 找到对应的 Access 会话
     */
    private suspend fun dispatchToAccess(message: Message<DataBuffer>) {
        val headers = message.headers
        val accessInstanceId = headers["accessInstanceId"] as? String

        if (accessInstanceId == null) {
            logger.warn("Missing accessInstanceId, message discarded")
            releaseMessageBuffer(message, "missing accessInstanceId")
            return
        }

        val accessSession = sessionManager.getSession(accessInstanceId)
        if (accessSession == null) {
            logger.warn("Access session not found: $accessInstanceId, message discarded")
            releaseMessageBuffer(message, "access session not found")
            return
        }

        if (accessSession.instanceType != "ACCESS") {
            logger.warn("Session is not ACCESS type: $accessInstanceId, message discarded")
            releaseMessageBuffer(message, "not ACCESS session")
            return
        }

        try {
            accessSession.sendMessage(message)
            logger.debug("Message dispatched to Access: ${accessSession.instanceId}")
        } catch (e: Exception) {
            logger.error("Failed to send message to Access ${accessSession.instanceId}: ${e.message}", e)
            releaseMessageBuffer(message, "send failed")
        }
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
     * 关闭服务，取消所有协程
     */
    fun shutdown() {
        logger.info("Stopping message dispatch service...")
        scope.cancel()
        logger.info("Message dispatch service stopped")
    }
}
