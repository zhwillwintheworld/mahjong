package com.sudooom.mahjong.common.route

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator

/**
 * 消息帧编解码器 将 RoutedMessage 编码为单个 ByteBuf，包含 metadata 和 payload
 *
 * 帧格式: [metadataLength: 2 bytes][metadata: N bytes][payload: M bytes]
 */
object MessageFrameCodec {

    /** 编码 RoutedMessage 为单个 ByteBuf */
    fun encode(
            message: RoutedMessage,
            allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT
    ): ByteBuf {
        val metadataBuf = RouteMetadataCodec.encode(message.routeMetadata, allocator)
        try {
            val metadataLength = metadataBuf.readableBytes()
            val payloadLength = message.payload.readableBytes()

            val frame = allocator.buffer(2 + metadataLength + payloadLength)
            frame.writeShort(metadataLength)
            frame.writeBytes(metadataBuf)
            frame.writeBytes(message.payload, message.payload.readerIndex(), payloadLength)

            return frame
        } finally {
            metadataBuf.release()
        }
    }

    /** 解码 ByteBuf 为 RoutedMessage 注意：返回的 payload 是原始 buffer 的 slice，不会增加引用计数 */
    fun decode(frame: ByteBuf): RoutedMessage {
        val metadataLength = frame.readUnsignedShort()
        val metadataBuf = frame.readSlice(metadataLength)
        val routeMetadata = RouteMetadataCodec.decode(metadataBuf)
        val payload = frame.slice() // 剩余部分是 payload

        return RoutedMessage(payload, routeMetadata)
    }

    /** 安全解码，失败返回 null */
    fun decodeOrNull(frame: ByteBuf): RoutedMessage? {
        return try {
            if (frame.readableBytes() < 2) return null
            decode(frame)
        } catch (e: Exception) {
            null
        }
    }
}
