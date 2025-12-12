@file:JvmName("ClientRequestDecoder")

package com.sudooom.mahjong.logic.codec

import com.google.protobuf.CodedInputStream
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.sudooom.mahjong.common.proto.ClientRequest
import io.netty.buffer.ByteBuf
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.NettyDataBuffer

/**
 * 缓存常用的 Parser 实例，避免重复获取
 */
private val CLIENT_REQUEST_PARSER: Parser<ClientRequest> = ClientRequest.parser()

/**
 * 默认消息大小限制：1MB，防止恶意大消息攻击
 */
private const val DEFAULT_SIZE_LIMIT = 1 * 1024 * 1024

/**
 * 将 DataBuffer 解码为 ClientRequest
 */
fun DataBuffer.toClientRequest(): ClientRequest {
    return toProto(CLIENT_REQUEST_PARSER)
}

/**
 * 通用方法：将 DataBuffer 解码为指定的 Protobuf 消息类型
 *
 * @param parser Protobuf 消息的 Parser
 * @param sizeLimit 消息大小限制（字节），默认 1MB
 */
fun <T : MessageLite> DataBuffer.toProto(
    parser: Parser<T>,
    sizeLimit: Int = DEFAULT_SIZE_LIMIT
): T {
    return when (this) {
        is NettyDataBuffer -> {
            // 直接使用底层 ByteBuf 进行解码（零拷贝）
            val byteBuf = this.nativeBuffer as ByteBuf
            byteBuf.parseProto(parser, sizeLimit)
        }

        else -> {
            // 非 Netty DataBuffer，使用 ByteBuffer 解码
            this.readableByteBuffers().use { iterator ->
                val byteBuffer = iterator.next()
                val input = CodedInputStream.newInstance(byteBuffer).apply {
                    setSizeLimit(sizeLimit)
                }
                parser.parseFrom(input)
            }
        }
    }
}

/**
 * 从 ByteBuf 解析 Protobuf 消息
 */
private fun <T : MessageLite> ByteBuf.parseProto(
    parser: Parser<T>,
    sizeLimit: Int
): T {
    val input = if (this.hasArray()) {
        // 堆内存 ByteBuf：直接使用底层数组（零拷贝）
        CodedInputStream.newInstance(
            this.array(),
            this.arrayOffset() + this.readerIndex(),
            this.readableBytes()
        )
    } else {
        // 直接内存 ByteBuf：使用 NIO ByteBuffer
        CodedInputStream.newInstance(this.nioBuffer())
    }
    input.setSizeLimit(sizeLimit)
    return parser.parseFrom(input)
}