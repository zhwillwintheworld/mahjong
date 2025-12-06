package com.sudooom.mahjong.broker.session

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.util.IdGenerator
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Component

/** 服务器会话管理器 管理所有连接到 Broker 的 Access/Logic 服务会话 */
@Component
class ServerSessionManager : Loggable {

    /** 按 requester 索引的会话 */
    private val sessions = ConcurrentHashMap<RSocketRequester, ServerSession>()

    /** 按 sessionId 索引的会话 */
    private val sessionIdMap = ConcurrentHashMap<String, ServerSession>()

    /** 按 instanceType 分组的会话（如 ACCESS、LOGIC） */
    private val instanceTypeSessions = ConcurrentHashMap<String, MutableSet<ServerSession>>()

    /**
     * 创建会话
     * @param instanceType 实例类型（ACCESS、LOGIC）
     * @param instanceId 实例 ID
     * @param requester RSocket 请求器
     * @return 创建的会话
     */
    fun createSession(
            instanceType: String,
            instanceId: String,
            requester: RSocketRequester
    ): ServerSession {
        val sessionId = IdGenerator.nextIdString()
        val session =
                ServerSession(
                        sessionId = sessionId,
                        instanceType = instanceType,
                        instanceId = instanceId,
                        requester = requester,
                        connectedAt = Instant.now(),
                        lastHeartbeat = Instant.now()
                )
        sessions[requester] = session
        sessionIdMap[sessionId] = session
        instanceTypeSessions.getOrPut(instanceType) { ConcurrentHashMap.newKeySet() }.add(session)
        logger.info(
                "创建服务器会话: instanceType=$instanceType, instanceId=$instanceId, sessionId=$sessionId"
        )
        return session
    }

    /** 根据 sessionId 获取会话 */
    fun getSession(sessionId: String): ServerSession? {
        return sessionIdMap[sessionId]
    }

    /** 根据 requester 获取会话 */
    fun getSession(requester: RSocketRequester): ServerSession? {
        return sessions[requester]
    }

    /** 获取指定类型的所有会话 */
    fun getSessionsByType(instanceType: String): Set<ServerSession> {
        return instanceTypeSessions[instanceType]?.toSet() ?: emptySet()
    }

    /** 移除会话 */
    fun removeSession(sessionId: String) {
        val session = sessionIdMap.remove(sessionId)
        if (session != null) {
            sessions.remove(session.requester)
            instanceTypeSessions[session.instanceType]?.remove(session)
            logger.info(
                    "移除服务器会话: instanceType=${session.instanceType}, instanceId=${session.instanceId}, sessionId=$sessionId"
            )
        }
    }

    /** 根据 requester 移除会话 */
    fun removeSession(requester: RSocketRequester) {
        val session = sessions.remove(requester)
        if (session != null) {
            sessionIdMap.remove(session.sessionId)
            instanceTypeSessions[session.instanceType]?.remove(session)
            logger.info(
                    "移除服务器会话: instanceType=${session.instanceType}, instanceId=${session.instanceId}, sessionId=${session.sessionId}"
            )
        }
    }

    /** 获取在线服务数量 */
    fun getOnlineCount(): Int {
        return sessionIdMap.size
    }

    /** 获取指定类型的在线服务数量 */
    fun getOnlineCountByType(instanceType: String): Int {
        return instanceTypeSessions[instanceType]?.size ?: 0
    }

    /** 获取所有在线实例 ID */
    fun getOnlineInstanceIds(): Set<String> {
        return sessionIdMap.values.map { it.instanceId }.toSet()
    }
}
