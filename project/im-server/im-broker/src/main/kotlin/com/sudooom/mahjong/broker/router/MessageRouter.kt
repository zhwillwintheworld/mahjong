package com.sudooom.mahjong.broker.router

import com.sudooom.mahjong.broker.session.ServerSession
import com.sudooom.mahjong.broker.session.ServerSessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.route.RouteMetadata
import com.sudooom.mahjong.common.route.RouteType
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.CRC32
import org.springframework.stereotype.Component

/** 消息路由器 根据 RouteMetadata 进行一致性哈希路由，选择目标 Logic 实例 */
@Component
class MessageRouter(private val sessionManager: ServerSessionManager) : Loggable {

    /** 虚拟节点数量，用于一致性哈希 */
    private val virtualNodes = 150

    /** 哈希环缓存 */
    private val hashRingCache = ConcurrentHashMap<String, List<Pair<Long, ServerSession>>>()

    /**
     * 根据路由信息选择目标 Logic 实例
     * @param metadata 路由元数据
     * @return 目标 Logic session 列表（广播时返回多个，其他情况返回单个）
     */
    fun route(metadata: RouteMetadata): List<ServerSession> {
        val logicSessions = sessionManager.getSessionsByType("LOGIC")
        if (logicSessions.isEmpty()) {
            logger.warn("No LOGIC instances available for routing")
            return emptyList()
        }

        return when (metadata.type) {
            RouteType.BROADCAST -> {
                // 广播：返回所有 Logic 实例
                logicSessions.toList()
            }
            RouteType.ROOM, RouteType.USER -> {
                // 一致性哈希选择单个实例
                val target = consistentHash(metadata.routeKey, logicSessions)
                if (target != null) listOf(target) else emptyList()
            }
            RouteType.UNKNOWN -> {
                // 未知类型：随机选择一个
                logger.warn("Unknown route type, randomly selecting LOGIC instance")
                listOf(logicSessions.random())
            }
        }
    }

    /** 一致性哈希选择目标实例 */
    private fun consistentHash(key: String, sessions: Set<ServerSession>): ServerSession? {
        if (sessions.isEmpty()) return null
        if (sessions.size == 1) return sessions.first()

        // 构建哈希环
        val ring = buildHashRing(sessions)
        if (ring.isEmpty()) return null

        // 计算 key 的哈希值
        val keyHash = hash(key)

        // 在环上找到第一个大于等于 keyHash 的节点
        val target =
                ring.firstOrNull { it.first >= keyHash }?.second ?: ring.first().second // 环形，回到第一个

        return target
    }

    /** 构建一致性哈希环 */
    private fun buildHashRing(sessions: Set<ServerSession>): List<Pair<Long, ServerSession>> {
        val ring = mutableListOf<Pair<Long, ServerSession>>()

        sessions.forEach { session ->
            // 为每个实例创建虚拟节点
            repeat(virtualNodes) { i ->
                val virtualKey = "${session.instanceId}#$i"
                val hash = hash(virtualKey)
                ring.add(hash to session)
            }
        }

        // 按哈希值排序
        return ring.sortedBy { it.first }
    }

    /** CRC32 哈希函数 */
    private fun hash(key: String): Long {
        val crc32 = CRC32()
        crc32.update(key.toByteArray())
        return crc32.value
    }

    /** 清除哈希环缓存（当 Logic 实例变化时调用） */
    fun invalidateCache() {
        hashRingCache.clear()
    }
}
