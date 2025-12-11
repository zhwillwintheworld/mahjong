package com.sudooom.mahjong.broker.service

import com.sudooom.mahjong.broker.session.ServerSessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.util.JwtUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.messaging.Message
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/** Broker 连接服务 处理来自 Access/Logic 服务的连接请求 使用 JWT 验证连接安全性 */
@Service
class BrokerConnectService(
    private val sessionManager: ServerSessionManager,
    private val jwtUtil: JwtUtil,
    private val messageDispatchService: MessageDispatchService
) : Loggable {

    /**
     * 处理连接请求
     * @param token JWT token（包含加密的 instanceType 和 instanceId）
     * @param requester RSocket 请求器
     */
    suspend fun connect(token: Mono<String>, requester: RSocketRequester) {
        val tokenValue =
            token.awaitSingleOrNull()
                ?: throw IllegalArgumentException("Token is required")

        // 解析并验证 JWT
        val claims =
            try {
                jwtUtil.parseToken(tokenValue)
            } catch (e: Exception) {
                logger.error("JWT 验证失败: ${e.message}")
                throw IllegalArgumentException("Invalid token: ${e.message}")
            }

        // 从 JWT 中提取 instanceType 和 instanceId
        val instanceType =
            jwtUtil.getClaim(claims, JwtUtil.CLAIM_INSTANCE_TYPE, String::class.java)
        val instanceId =
            jwtUtil.getClaim(claims, JwtUtil.CLAIM_INSTANCE_ID, String::class.java)

        logger.info("JWT 验证成功: instanceType=$instanceType, instanceId=$instanceId")

        sessionManager.createSession(
            instanceType = instanceType,
            instanceId = instanceId,
            requester = requester
        )
        logger.info(
            "服务连接成功: instanceType=$instanceType, instanceId=$instanceId"
        )

        // 监听连接关闭事件
        requester
            .rsocket()
            ?.onClose()
            ?.doOnTerminate {
                logger.info(
                    "服务断开连接: instanceType=$instanceType, instanceId=$instanceId"
                )
                sessionManager.removeSession(requester)
            }
            ?.subscribe()
    }

    /**
     * 处理消息通道
     * @param messages 输入消息流
     * @param requester RSocket 请求器
     * @return 输出消息流
     */
    suspend fun channel(
        messages: Flow<Message<DataBuffer>>,
        requester: RSocketRequester
    ): Flow<Message<DataBuffer>> {
        val session =
            sessionManager.getSession(requester)
                ?: throw IllegalArgumentException(
                    "Session not found, please connect first"
                )
        logger.debug("消息通道已建立: instanceId=${session.instanceId}")

        // 订阅消息流并分发到目标实例
        messageDispatchService.subscribeAndDispatch(messages, session)

        return session.getMessageFlow()
    }
}
