package com.sudooom.mahjong.broker.service

import com.sudooom.mahjong.broker.holder.BrokerInboundHolder
import com.sudooom.mahjong.broker.router.MessageRouter
import com.sudooom.mahjong.broker.session.ServerSessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.constant.MessageHeaders
import com.sudooom.mahjong.common.route.RouteMetadata
import com.sudooom.mahjong.common.route.RouteType
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

/**
 * 消息分发服务 - Channel + 多 Worker 模式
 *
 * 架构：
 * BrokerInboundHolder (SharedFlow) -> dispatchChannel (Channel) -> N 个 Worker 并发消费
 *
 * 优势：
 * - 高吞吐：多个 Worker 并发处理消息
 * - 负载均衡：Channel 自动在 Worker 间分发消息
 * - 资源可控：Worker 数量固定，不会无限制创建协程
 */
@Service
class MessageDispatchService(
    private val sessionManager: ServerSessionManager,
    private val messageRouter: MessageRouter,
) : Loggable {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Worker 数量 = CPU 核心数 * 2
    private val workerCount = Runtime.getRuntime().availableProcessors() * 2

    // 内部分发 Channel，用于多 Worker 消费
    private val dispatchChannel = Channel<Message<DataBuffer>>(capacity = Channel.UNLIMITED)

    /**
     * 应用启动后：
     * 1. 启动生产者协程：从 SharedFlow 读取消息放入 Channel
     * 2. 启动多个 Worker 协程：并发消费 Channel 中的消息
     */
    @PostConstruct
    fun startDispatching() {
        logger.info("Starting broker message dispatch service with $workerCount workers...")

        // 1. 生产者：从 SharedFlow 读取并放入 Channel
        scope.launch {
            BrokerInboundHolder.asFlow()
                .collect { message ->
                    dispatchChannel.send(message)
                }
        }

        // 2. 启动多个 Worker 消费者
        repeat(workerCount) { workerId ->
            scope.launch {
                logger.debug("Worker-$workerId started")
                for (message in dispatchChannel) {
                    try {
                        dispatchMessage(message)
                    } catch (e: Exception) {
                        logger.error("Worker-$workerId failed: ${e.message}", e)
                        releaseMessageBuffer(message, "dispatch failed")
                    }
                }
                logger.debug("Worker-$workerId stopped")
            }
        }

        logger.info("Broker message dispatch service started successfully")
    }

    /**
     * 根据消息 header 中的 instanceType 分发消息
     */
    private suspend fun dispatchMessage(message: Message<DataBuffer>) {
        when (val instanceType = message.headers[MessageHeaders.INSTANCE_TYPE] as? String) {
            MessageHeaders.InstanceType.ACCESS -> dispatchToLogic(message)
            MessageHeaders.InstanceType.LOGIC -> dispatchToAccess(message)
            else -> {
                logger.warn("Unknown or missing instanceType: $instanceType, message discarded")
                releaseMessageBuffer(message, "unknown instance type")
            }
        }
    }

    /**
     * 将消息路由到 Logic 实例
     */
    private suspend fun dispatchToLogic(message: Message<DataBuffer>) {
        val headers = message.headers
        val routeKey = MessageHeaders.getRouteKey(headers)

        if (routeKey == null) {
            logger.warn("Missing route key, message discarded")
            releaseMessageBuffer(message, "missing route key")
            return
        }

        val routeType = if (MessageHeaders.isRoomRoute(headers)) RouteType.ROOM else RouteType.USER
        val metadata = RouteMetadata(routeType, routeKey)
        val targetSession = messageRouter.route(metadata)

        if (targetSession == null) {
            logger.warn("No Logic instances available")
            releaseMessageBuffer(message, "no Logic instances")
            return
        }

        try {
            targetSession.sendMessage(message)
            logger.debug("Dispatched to Logic: ${targetSession.instanceId}")
        } catch (e: Exception) {
            logger.error("Failed to send to Logic ${targetSession.instanceId}: ${e.message}", e)
        }
    }

    /**
     * 将消息路由到 Access 实例
     */
    private suspend fun dispatchToAccess(message: Message<DataBuffer>) {
        val accessInstanceId = message.headers[MessageHeaders.TO_INSTANCE_ID] as? String

        if (accessInstanceId == null) {
            logger.warn("Missing toInstanceId, message discarded")
            releaseMessageBuffer(message, "missing toInstanceId")
            return
        }

        val accessSession = sessionManager.getSession(accessInstanceId)
        if (accessSession == null || accessSession.instanceType != "ACCESS") {
            logger.warn("Access session not found or invalid: $accessInstanceId")
            releaseMessageBuffer(message, "session not found")
            return
        }

        try {
            accessSession.sendMessage(message)
            logger.debug("Dispatched to Access: ${accessSession.instanceId}")
        } catch (e: Exception) {
            logger.error("Failed to send to Access ${accessSession.instanceId}: ${e.message}", e)
            releaseMessageBuffer(message, "send failed")
        }
    }

    private fun releaseMessageBuffer(message: Message<DataBuffer>, reason: String) {
        logger.debug("Releasing buffer: $reason")
        DataBufferUtils.release(message.payload)
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Stopping broker message dispatch service...")
        dispatchChannel.close()  // 关闭 Channel，所有 Worker 会结束
        scope.cancel()
        logger.info("Broker message dispatch service stopped")
    }
}


