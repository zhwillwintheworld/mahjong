package com.sudooom.mahjong.common.route

/**
 * 路由元数据 用于在 RSocket 消息中携带路由信息
 *
 * @param type 路由类型
 * @param routeKey 路由键（roomId 或 userId）
 */
data class RouteMetadata(val type: RouteType, val routeKey: String) {
    companion object {
        /** 创建房间路由 */
        fun room(roomId: String) = RouteMetadata(RouteType.ROOM, roomId)

        /** 创建用户路由 */
        fun user(userId: String) = RouteMetadata(RouteType.USER, userId)

        /** 创建逻辑路由 */
        fun logic(accessId: String) = RouteMetadata(RouteType.LOGIC, accessId)
    }
}
