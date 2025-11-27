package com.sudooom.mahjong.data.mongo.repository

import com.sudooom.mahjong.data.mongo.entity.Group
import com.sudooom.mahjong.data.mongo.entity.Message
import com.sudooom.mahjong.data.mongo.entity.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/** 用户仓储 */
@Repository
interface UserRepository : ReactiveMongoRepository<User, String> {
    fun findByUsername(username: String): Mono<User>
    fun findByEmail(email: String): Mono<User>
}

/** 消息仓储 */
@Repository
interface MessageRepository : ReactiveMongoRepository<Message, String> {
    fun findByFromUserIdOrToUserIdOrderByTimestampDesc(
            fromUserId: String,
            toUserId: String
    ): Flux<Message>

    fun findByGroupIdOrderByTimestampDesc(groupId: String): Flux<Message>
}

/** 群组仓储 */
@Repository
interface GroupRepository : ReactiveMongoRepository<Group, String> {
    fun findByOwnerId(ownerId: String): Flux<Group>
}
