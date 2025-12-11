package com.sudooom.mahjong.broker.router

import com.sudooom.mahjong.broker.session.ServerSession
import com.sudooom.mahjong.broker.session.ServerSessionManager
import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.route.RouteMetadata
import com.sudooom.mahjong.common.route.RouteType.*
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.CRC32

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
    fun route(metadata: RouteMetadata): ServerSession? {
        val onlineLogicSessions = sessionManager.getOnlineLogicSessions()
        if (onlineLogicSessions.isEmpty()) {
            logger.warn("No LOGIC instances available for routing")
            return null
        }

        return when (metadata.type) {
            ROOM, USER -> {
                // 一致性哈希选择单个实例
                consistentHash(metadata.routeKey, onlineLogicSessions)
            }

            LOGIC -> {
                // 直接路由到指定 Logic
                val session = sessionManager.getSession(metadata.routeKey)
                if (session?.canAcceptNewRequests() == true) {
                    session
                } else {
                    logger.warn("Target Logic ${metadata.routeKey} is not available")
                    null
                }
            }

            UNKNOWN -> null
        }
    }

    /** 一致性哈希选择目标实例 */
    private fun consistentHash(key: String, sessions: Set<ServerSession>): ServerSession? {
        if (sessions.isEmpty()) return null
        if (sessions.size == 1) return sessions.first()

        // 使用实例列表的签名作为缓存 key
        val cacheKey = buildCacheKey(sessions)

        // 尝试从缓存获取哈希环
        val ring = hashRingCache.getOrPut(cacheKey) {
            logger.debug("构建新的哈希环，实例数量: ${sessions.size}")
            buildHashRing(sessions)
        }

        if (ring.isEmpty()) return null

        // 计算 key 的哈希值
        val keyHash = hash(key)

        // 在环上找到第一个大于等于 keyHash 的节点
        val target =
            ring.firstOrNull { it.first >= keyHash }?.second ?: ring.first().second // 环形，回到第一个

        return target
    }

    /** 构建缓存 key，使用所有实例 ID 的排序字符串 */
    private fun buildCacheKey(sessions: Set<ServerSession>): String {
        return sessions
            .map { it.instanceId }
            .sorted()
            .joinToString(",")
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
        if (hashRingCache.isNotEmpty()) {
            logger.info("清除哈希环缓存，缓存条目数: ${hashRingCache.size}")
            hashRingCache.clear()
        }
    }
}
