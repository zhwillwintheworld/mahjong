package com.sudooom.mahjong.common.codec

import com.google.protobuf.Parser
import com.sudooom.mahjong.common.proto.ClientRequest
import com.sudooom.mahjong.common.proto.ServerMessage
import org.springframework.core.io.buffer.DataBuffer

/**
 * 消息解码器聚类
 * 提供所有 Protobuf 消息类型的解码扩展函数
 */

// ============================================================================
// Parser 缓存
// ============================================================================

/**
 * 缓存 ClientRequest Parser 实例，避免重复获取
 */
private val CLIENT_REQUEST_PARSER: Parser<ClientRequest> = ClientRequest.parser()

/**
 * 缓存 ServerMessage Parser 实例，避免重复获取
 */
private val SERVER_MESSAGE_PARSER: Parser<ServerMessage> = ServerMessage.parser()

// ============================================================================
// 解码扩展函数
// ============================================================================

/**
 * 将 DataBuffer 解码为 ClientRequest（零拷贝）
 */
fun DataBuffer.toClientRequest(): ClientRequest {
    return toProto(CLIENT_REQUEST_PARSER)
}

/**
 * 将 DataBuffer 解码为 ServerMessage（零拷贝）
 */
fun DataBuffer.toServerMessage(): ServerMessage {
    return toProto(SERVER_MESSAGE_PARSER)
}
