package com.sudooom.mahjong.access.session

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.util.IdGenerator
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/** 会话管理器 管理所有用户会话 */
@Component
class SessionManager : Loggable {

    private val sessions = ConcurrentHashMap<RSocketRequester, UserSession>()

    private val sessionIdMap = ConcurrentHashMap<String, UserSession>()

    /** 创建会话 */
    fun createSession(userId: String, deviceId: String, platform: String, requester: RSocketRequester): UserSession {
        val sessionId = IdGenerator.nextIdString()
        val session = UserSession(
            sessionId = sessionId,
            deviceId = deviceId,
            platform = platform,
            userId = userId,
            requester = requester,
            connectedAt = Instant.now(),
            lastHeartbeat = Instant.now()
        )
        sessions[requester] = session
        sessionIdMap[sessionId] = session
        logger.info("创建会话: userId=$userId, sessionId=$sessionId")
        return session
    }

    /** 获取会话 */
    fun getSession(sessionId: String): UserSession? {
        return sessionIdMap[sessionId]
    }

    /** 获取会话 */
    fun getSession(requester: RSocketRequester): UserSession? {
        return sessions[requester]
    }

    /** 移除会话 - 通过 sessionId */
    fun removeSession(sessionId: String) {
        val session = sessionIdMap.remove(sessionId)
        if (session != null) {
            sessions.remove(session.requester)
            logger.info("移除会话: userId=${session.userId}, sessionId=${session.sessionId}")
        }
    }

    /** 移除会话 - 通过 RSocketRequester */
    fun removeSession(requester: RSocketRequester) {
        val session = sessions.remove(requester)
        if (session != null) {
            sessionIdMap.remove(session.sessionId)
            logger.info("移除会话: userId=${session.userId}, sessionId=${session.sessionId}")
        }
    }

    /** 在线用户数 */
    fun getOnlineUserCount(): Int {
        return sessionIdMap.values.map { it.userId }.toSet().size
    }

    /** 获取所有在线用户 ID */
    fun getOnlineUserIds(): Set<String> {
        return sessionIdMap.values.map { it.userId }.toSet()
    }
}
