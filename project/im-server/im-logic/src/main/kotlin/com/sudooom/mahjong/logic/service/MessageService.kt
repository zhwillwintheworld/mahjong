package com.sudooom.mahjong.logic.service

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.util.IdGenerator
import com.sudooom.mahjong.data.mongo.entity.Message
import com.sudooom.mahjong.data.mongo.repository.MessageRepository
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service

/** 消息服务 */
@Service
class MessageService(private val messageRepository: MessageRepository) : Loggable {

    /** 保存消息 */
    suspend fun saveMessage(message: Message): Message {
        return messageRepository.save(message).awaitFirst()
    }

    /** 创建消息 */
    suspend fun createMessage(
            fromUserId: String,
            toUserId: String?,
            groupId: String?,
            type: Int,
            content: String,
            ext: Map<String, String>? = null
    ): Message {
        val message =
                Message(
                        id = IdGenerator.nextIdString(),
                        fromUserId = fromUserId,
                        toUserId = toUserId,
                        groupId = groupId,
                        type = type,
                        content = content,
                        timestamp = System.currentTimeMillis(),
                        ext = ext
                )

        return saveMessage(message)
    }

    /** 获取用户消息历史 */
    suspend fun getUserMessages(userId1: String, userId2: String): List<Message> {
        return messageRepository
                .findByFromUserIdOrToUserIdOrderByTimestampDesc(userId1, userId2)
                .collectList()
                .awaitFirst()
    }
}
