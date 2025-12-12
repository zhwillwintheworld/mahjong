@file:JvmName("ClientResponseEncoder")

package com.sudooom.mahjong.logic.codec

import com.google.protobuf.MessageLite
import com.sudooom.mahjong.common.proto.ClientResponse
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.NettyDataBufferFactory

/**
 * 默认的 DataBufferFactory，使用 Netty 的池化 Allocator（零拷贝友好）
 */
private val DEFAULT_BUFFER_FACTORY: DataBufferFactory =
    NettyDataBufferFactory(ByteBufAllocator.DEFAULT)

/**
 * 将 ClientResponse 编码为 DataBuffer
 */
fun ClientResponse.toDataBuffer(
    bufferFactory: DataBufferFactory = DEFAULT_BUFFER_FACTORY
): DataBuffer {
    return toProtoBuffer(bufferFactory)
}

/**
 * 通用方法：将 Protobuf 消息编码为 DataBuffer
 *
 * 针对 Netty 环境优化，实现零拷贝：
 * - 使用 NettyDataBufferFactory 时，直接写入底层 ByteBuf
 * - 使用其他 DataBufferFactory 时，先序列化到 byte[] 再写入
 *
 * @param bufferFactory DataBuffer 工厂，默认使用 Netty 池化 Allocator
 */
fun MessageLite.toProtoBuffer(
    bufferFactory: DataBufferFactory = DEFAULT_BUFFER_FACTORY
): DataBuffer {
    val size = this.serializedSize

    return when (bufferFactory) {
        is NettyDataBufferFactory -> {
            // Netty 环境：直接写入 ByteBuf（零拷贝）
            val buffer = bufferFactory.allocateBuffer(size)
            val byteBuf = buffer.nativeBuffer as ByteBuf
            byteBuf.writeProto(this, size)
            buffer
        }

        else -> {
            // 非 Netty 环境：序列化到 byte[] 后写入
            val bytes = this.toByteArray()
            val buffer = bufferFactory.allocateBuffer(bytes.size)
            buffer.write(bytes)
            buffer
        }
    }
}

/**
 * 将 Protobuf 消息直接写入 ByteBuf（零拷贝）
 *
 * 使用 CodedOutputStream 直接写入 ByteBuf 的底层内存，
 * 避免中间 byte[] 分配。
 *
 * @param message 要写入的 Protobuf 消息
 * @param size 消息序列化后的大小（已预先计算，避免重复计算）
 */
private fun ByteBuf.writeProto(message: MessageLite, size: Int) {
    this.ensureWritable(size)

    if (this.hasArray()) {
        // 堆内存 ByteBuf：直接写入底层数组
        message.writeTo(
            com.google.protobuf.CodedOutputStream.newInstance(
                this.array(),
                this.arrayOffset() + this.writerIndex(),
                size
            )
        )
        this.writerIndex(this.writerIndex() + size)
    } else {
        // 直接内存 ByteBuf：使用 NIO ByteBuffer 写入
        val nioBuffer = this.nioBuffer(this.writerIndex(), size)
        message.writeTo(
            com.google.protobuf.CodedOutputStream.newInstance(nioBuffer)
        )
        this.writerIndex(this.writerIndex() + size)
    }
}

