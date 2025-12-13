package com.sudooom.mahjong.common.codec

import com.google.protobuf.CodedInputStream
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.NettyDataBuffer
import org.springframework.core.io.buffer.NettyDataBufferFactory

/**
 * Protobuf 编解码通用基础设施
 * 提供零拷贝的编码和解码方法
 */

// ============================================================================
// 解码相关
// ============================================================================

/**
 * 默认消息大小限制：1MB，防止恶意大消息攻击
 */
private const val DEFAULT_SIZE_LIMIT = 1 * 1024 * 1024

/**
 * 通用方法：将 DataBuffer 解码为指定的 Protobuf 消息类型
 *
 * 支持零拷贝解码：
 * - 对于 NettyDataBuffer，直接使用底层 ByteBuf
 * - 对于非 Netty DataBuffer，使用 ByteBuffer
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
 * 从 ByteBuf 解析 Protobuf 消息（零拷贝）
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

// ============================================================================
// 编码相关
// ============================================================================

/**
 * 默认的 DataBufferFactory，使用 Netty 的池化 Allocator（零拷贝友好）
 */
private val DEFAULT_BUFFER_FACTORY: DataBufferFactory =
    NettyDataBufferFactory(ByteBufAllocator.DEFAULT)

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

