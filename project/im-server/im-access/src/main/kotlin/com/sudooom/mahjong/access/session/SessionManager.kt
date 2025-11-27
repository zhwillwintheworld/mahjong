package com.sudooom.mahjong.access.session

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.util.IdGenerator
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Component

/** 会话管理器 管理所有用户会话 */
@Component
class SessionManager : Loggable {

    // userId -> UserSession
    private val sessions = ConcurrentHashMap<String, UserSession>()

    // sessionId -> userId
    private val sessionIdMap = ConcurrentHashMap<String, String>()

    /** 创建会话 */
    fun createSession(userId: String, requester: RSocketRequester): UserSession {
        val sessionId = IdGenerator.nextIdString()
        val session = UserSession(userId, sessionId, requester)

        // 移除旧会话
        removeSession(userId)

        sessions[userId] = session
        sessionIdMap[sessionId] = userId

        logger.info("创建会话: userId=$userId, sessionId=$sessionId")
        return session
    }

    /** 获取会话 */
    fun getSession(userId: String): UserSession? {
        return sessions[userId]
    }

    /** 根据 sessionId 获取会话 */
    fun getSessionBySessionId(sessionId: String): UserSession? {
        val userId = sessionIdMap[sessionId] ?: return null
        return sessions[userId]
    }

    /** 移除会话 */
    fun removeSession(userId: String) {
        val session = sessions.remove(userId)
        if (session != null) {
            sessionIdMap.remove(session.sessionId)
            logger.info("移除会话: userId=$userId, sessionId=${session.sessionId}")
        }
    }

    /** 获取用户消息流 */
    fun getMessageFlow(userId: String): Flow<ByteArray> {
        return getSession(userId)?.messageFlow ?: emptyFlow()
    }

    /** 是否在线 */
    fun isOnline(userId: String): Boolean {
        return sessions.containsKey(userId)
    }

    /** 在线用户数 */
    fun getOnlineUserCount(): Int {
        return sessions.size
    }

    /** 获取所有在线用户 ID */
    fun getOnlineUserIds(): Set<String> {
        return sessions.keys.toSet()
    }
}
