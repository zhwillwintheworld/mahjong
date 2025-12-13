package com.sudooom.mahjong.common.codec

import com.sudooom.mahjong.common.proto.ClientResponse
import com.sudooom.mahjong.common.proto.ServerMessage
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory

/**
 * 消息编码器聚类
 * 提供所有 Protobuf 消息类型的编码扩展函数
 */

/**
 * 将 ClientResponse 编码为 DataBuffer（零拷贝）
 */
fun ClientResponse.toDataBuffer(
    bufferFactory: DataBufferFactory? = null
): DataBuffer {
    return if (bufferFactory != null) {
        toProtoBuffer(bufferFactory)
    } else {
        toProtoBuffer()
    }
}

/**
 * 将 ServerMessage 编码为 DataBuffer（零拷贝）
 */
fun ServerMessage.toDataBuffer(
    bufferFactory: DataBufferFactory? = null
): DataBuffer {
    return if (bufferFactory != null) {
        toProtoBuffer(bufferFactory)
    } else {
        toProtoBuffer()
    }
}

