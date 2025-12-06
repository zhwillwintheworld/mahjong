package com.sudooom.mahjong.common.route

import com.sudooom.mahjong.common.proto.ServerMessage
/**
 * 带路由信息的消息 包装 payload 和路由元数据
 *
 * @param payload 消息内容
 * @param routeMetadata 路由信息
 */
data class RoutedMessage(val payload: ServerMessage, val routeMetadata: RouteMetadata) {
    companion object {
        /** 创建房间路由消息 */
        fun room(roomId: String, payload: ServerMessage) =
            RoutedMessage(payload, RouteMetadata.room(roomId))

        /** 创建用户路由消息 */
        fun user(userId: String, payload: ServerMessage) =
            RoutedMessage(payload, RouteMetadata.user(userId))

        /** 创建逻辑消息 */
        fun logic(payload: ServerMessage, accessId: String) = RoutedMessage(payload, RouteMetadata.logic(accessId))
    }
}
