package com.sudooom.mahjong.data.mongo.entity

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/** 用户实体 */
@Document(collection = "users")
data class User(
        @Id val id: String,
        val username: String,
        val password: String,
        val email: String? = null,
        val phone: String? = null,
        val avatar: String? = null,
        val nickname: String? = null,
        val status: Int = 0, // 0-离线 1-在线
        val createdAt: LocalDateTime = LocalDateTime.now(),
        val updatedAt: LocalDateTime = LocalDateTime.now()
)

/** 消息实体 */
@Document(collection = "messages")
data class Message(
        @Id val id: String,
        val fromUserId: String,
        val toUserId: String? = null,
        val groupId: String? = null,
        val type: Int, // 消息类型
        val content: String,
        val timestamp: Long,
        val ext: Map<String, String>? = null,
        val createdAt: LocalDateTime = LocalDateTime.now()
)

/** 群组实体 */
@Document(collection = "groups")
data class Group(
        @Id val id: String,
        val name: String,
        val avatar: String? = null,
        val ownerId: String,
        val memberIds: List<String> = emptyList(),
        val createdAt: LocalDateTime = LocalDateTime.now(),
        val updatedAt: LocalDateTime = LocalDateTime.now()
)
