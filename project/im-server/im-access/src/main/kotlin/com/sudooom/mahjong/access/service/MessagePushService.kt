package com.sudooom.mahjong.access.service

import com.sudooom.mahjong.access.session.SessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 消息推送服务
 */
@Service
class MessagePushService(
    private val sessionManager: SessionManager
): Loggable {
    
    private val scope = CoroutineScope(Dispatchers.Default)
    
    /**
     * 推送消息给指定用户
     */
    suspend fun pushToUser(userId: String, message: ByteArray): Boolean {
        val session = sessionManager.getSession(userId)
        
        return if (session != null) {
            try {
                session.sendMessage(message)
                logger.debug("推送消息给用户: $userId, size=${message.size}")
                true
            } catch (e: Exception) {
                logger.error("推送消息失败: userId=$userId", e)
                false
            }
        } else {
            logger.warn("用户不在线: $userId")
            false
        }
    }
    
    /**
     * 推送消息给多个用户
     */
    fun pushToUsers(userIds: Set<String>, message: ByteArray) {
        scope.launch {
            userIds.forEach { userId ->
                pushToUser(userId, message)
            }
        }
    }
    
    /**
     * 广播消息给所有在线用户
     */
    fun broadcast(message: ByteArray) {
        val onlineUsers = sessionManager.getOnlineUserIds()
        pushToUsers(onlineUsers, message)
        logger.info("广播消息: 在线用户数=${onlineUsers.size}")
    }
}
