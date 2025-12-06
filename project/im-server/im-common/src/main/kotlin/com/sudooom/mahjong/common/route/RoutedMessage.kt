package com.sudooom.mahjong.common.route

import io.netty.buffer.ByteBuf

/**
 * 带路由信息的消息 包装 payload 和路由元数据
 *
 * @param payload 消息内容
 * @param routeMetadata 路由信息
 */
data class RoutedMessage(val payload: ByteBuf, val routeMetadata: RouteMetadata) {
    companion object {
        /** 创建房间路由消息 */
        fun room(roomId: String, payload: ByteBuf) =
                RoutedMessage(payload, RouteMetadata.room(roomId))

        /** 创建用户路由消息 */
        fun user(userId: String, payload: ByteBuf) =
                RoutedMessage(payload, RouteMetadata.user(userId))

        /** 创建广播消息 */
        fun broadcast(payload: ByteBuf) = RoutedMessage(payload, RouteMetadata.broadcast())
    }
}
