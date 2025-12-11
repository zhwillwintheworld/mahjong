package com.sudooom.mahjong.logic.service

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.proto.*
import com.sudooom.mahjong.common.util.IdGenerator
import com.sudooom.mahjong.core.holder.BrokerOutboundHolder
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.codec.protobuf.ProtobufEncoder
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * 消息处理服务
 * 构建响应消息并通过 protobuf 编码后发送回 broker
 */
@Service
class MessageProcessService : Loggable {

    private val protobufEncoder = ProtobufEncoder()
    private val bufferFactory = DefaultDataBufferFactory()

    /**
     * 发送聊天确认响应
     */
    suspend fun sendChatAck(
        reqId: String,
        sessionId: String,
        accessInstanceId: String
    ) {
        val msgId = IdGenerator.nextIdString()
        val response = clientResponse {
            this.reqId = reqId
            timestamp = System.currentTimeMillis()
            code = ErrorCode.SUCCESS
            msg = "Message sent successfully"
            chatSendAck = chatSendAck {
                this.msgId = msgId
                sendTime = System.currentTimeMillis()
            }
        }

        sendResponse(response, sessionId, accessInstanceId)
    }

    /**
     * 发送房间响应
     */
    suspend fun sendRoomResponse(
        reqId: String,
        sessionId: String,
        accessInstanceId: String,
        roomId: String
    ) {
        // TODO: 从数据库或缓存获取实际的房间信息
        val response = clientResponse {
            this.reqId = reqId
            timestamp = System.currentTimeMillis()
            code = ErrorCode.SUCCESS
            msg = "Room operation successful"
            roomResp = roomResp {
                this.roomId = roomId
                // TODO: 设置 roomInfo
            }
        }

        sendResponse(response, sessionId, accessInstanceId)
    }

    /**
     * 发送心跳响应
     */
    suspend fun sendHeartbeatResponse(
        reqId: String,
        sessionId: String,
        accessInstanceId: String
    ) {
        val response = clientResponse {
            this.reqId = reqId
            timestamp = System.currentTimeMillis()
            code = ErrorCode.SUCCESS
            msg = "Heartbeat"
            heartbeatResp = heartbeatResp {
                serverTime = System.currentTimeMillis()
            }
        }

        sendResponse(response, sessionId, accessInstanceId)
    }

    /**
     * 发送错误响应
     */
    suspend fun sendErrorResponse(
        reqId: String,
        sessionId: String,
        accessInstanceId: String,
        errorCode: ErrorCode,
        errorMsg: String
    ) {
        val response = clientResponse {
            this.reqId = reqId
            timestamp = System.currentTimeMillis()
            code = errorCode
            msg = errorMsg
        }

        sendResponse(response, sessionId, accessInstanceId)
    }

    /**
     * 推送聊天消息到用户
     */
    suspend fun pushChatMessage(
        targetSessionId: String,
        accessInstanceId: String,
        senderId: String,
        chatType: ChatType,
        targetId: String,
        msgType: MsgType,
        content: String
    ) {
        val msgId = IdGenerator.nextIdString()
        val response = clientResponse {
            reqId = ""
            timestamp = System.currentTimeMillis()
            code = ErrorCode.SUCCESS
            msg = "Chat push"
            chatPush = chatPush {
                this.msgId = msgId
                this.senderId = senderId
                this.chatType = chatType
                this.targetId = targetId
                this.msgType = msgType
                this.content = content
                sendTime = System.currentTimeMillis()
            }
        }

        sendResponse(response, targetSessionId, accessInstanceId)
    }

    /**
     * 通用响应发送方法
     * 使用 protobuf 编码 ClientResponse 为 DataBuffer 并发送
     */
    private suspend fun sendResponse(
        response: ClientResponse,
        sessionId: String,
        accessInstanceId: String
    ) {
        try {
            // 使用 ProtobufEncoder 编码 ClientResponse 为 DataBuffer
            val dataBuffer = encodeProtobufMessage(response)

            // 构建消息并设置路由元数据
            val message = MessageBuilder.withPayload(dataBuffer)
                .setHeader("sessionId", sessionId)
                .setHeader("accessInstanceId", accessInstanceId)
                .build()

            // 发送到 Broker
            BrokerOutboundHolder.send(message)

            logger.debug("Response sent: reqId=${response.reqId}, to session=$sessionId")

        } catch (e: Exception) {
            logger.error("Failed to send response: ${e.message}", e)
        }
    }

    /**
     * 编码 protobuf 消息为 DataBuffer
     */
    private fun encodeProtobufMessage(message: com.google.protobuf.MessageLite): DataBuffer {
        val buffer = bufferFactory.allocateBuffer()

        protobufEncoder.encode(
            Mono.just(message),
            buffer,
            ResolvableType.forClass(message::class.java),
            null,
            null
        ).subscribe()

        return buffer
    }
}
