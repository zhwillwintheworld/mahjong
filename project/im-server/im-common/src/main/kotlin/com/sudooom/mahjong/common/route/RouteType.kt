package com.sudooom.mahjong.common.route

/** 路由类型 用于 Broker 对 Logic 进行一致性哈希路由 */
enum class RouteType(val code: Byte) {
    /** 未知类型 */
    UNKNOWN(0),

    /** 房间路由，使用 roomId 作为 route_key */
    ROOM(1),

    /** 用户路由，使用 userId 作为 route_key */
    USER(2),

    /** 广播，发送给所有 Logic */
    BROADCAST(3);

    companion object {
        fun fromCode(code: Byte): RouteType {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}
