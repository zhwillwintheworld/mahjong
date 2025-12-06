package com.sudooom.mahjong.common.route

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import java.nio.charset.StandardCharsets

/** 路由元数据编解码器 二进制格式: [type: 1 byte][routeKeyLength: 2 bytes][routeKey: N bytes] */
object RouteMetadataCodec {

    /** 元数据 MIME 类型 */
    const val MIME_TYPE = "application/x-route-metadata"

    /** 编码 RouteMetadata 为 ByteBuf */
    fun encode(
            metadata: RouteMetadata,
            allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT
    ): ByteBuf {
        val routeKeyBytes = metadata.routeKey.toByteArray(StandardCharsets.UTF_8)
        val buffer = allocator.buffer(1 + 2 + routeKeyBytes.size)

        buffer.writeByte(metadata.type.code.toInt())
        buffer.writeShort(routeKeyBytes.size)
        buffer.writeBytes(routeKeyBytes)

        return buffer
    }

    /** 解码 ByteBuf 为 RouteMetadata */
    fun decode(buffer: ByteBuf): RouteMetadata {
        val typeCode = buffer.readByte()
        val routeKeyLength = buffer.readUnsignedShort()
        val routeKeyBytes = ByteArray(routeKeyLength)
        buffer.readBytes(routeKeyBytes)

        return RouteMetadata(
                type = RouteType.fromCode(typeCode),
                routeKey = String(routeKeyBytes, StandardCharsets.UTF_8)
        )
    }

    /** 安全解码，失败返回 null */
    fun decodeOrNull(buffer: ByteBuf): RouteMetadata? {
        return try {
            if (buffer.readableBytes() < 3) return null
            decode(buffer)
        } catch (e: Exception) {
            null
        }
    }
}
