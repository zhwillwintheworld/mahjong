package com.sudooom.mahjong.access.service

import com.sudooom.mahjong.access.session.SessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.core.holder.BrokerInboundHolder
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
 * 从 Broker 接收消息并根据 sessionId 路由到对应的用户会话
 * 使用 Semaphore 限制并发数，充分利用 CPU 资源同时避免过载
 */
@Service
class MessageDispatchService(
    private val sessionManager: SessionManager,
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
        logger.info("Starting access message dispatch service with concurrency limit: $concurrencyLimit")

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

        logger.info("Access message dispatch service started successfully")
    }

    /**
     * 根据消息的 sessionId 分发到对应的用户会话
     */
    private suspend fun dispatchMessage(message: Message<DataBuffer>) {
        val sessionId = message.headers["sessionId"] as? String

        if (sessionId == null) {
            logger.warn("Missing sessionId, message discarded")
            releaseMessageBuffer(message, "no sessionId")
            return
        }

        val session = sessionManager.getSession(sessionId)
        if (session == null) {
            logger.warn("Session not found: $sessionId, message discarded")
            releaseMessageBuffer(message, "session not found")
            return
        }

        session.sendMessage(message)
        logger.debug("Message dispatched to session: $sessionId")
    }

    private fun releaseMessageBuffer(message: Message<DataBuffer>, reason: String) {
        logger.debug("Releasing buffer: $reason")
        DataBufferUtils.release(message.payload)
    }

    @PreDestroy
    fun stopDispatching() {
        logger.info("Stopping access message dispatch service...")
        scope.cancel()
        logger.info("Access message dispatch service stopped")
    }
}

