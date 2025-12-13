package com.sudooom.mahjong.common.constant

/**
 * Message Header 常量定义 - 极限压缩版本
 *
 * 使用数字索引作为 header 键，实现最极限的压缩
 * 同时提供枚举值压缩，实现两级压缩（键 + 值）
 *
 * 设计原则：
 * - 只传输必要的信息，可推导的不传输
 * - routeKey/routeType 从 roomId 是否存在推导
 * - requestId 放在 protobuf payload 中
 *
 * Header 列表（共 7 个）：
 * - 0: sessionId  - 会话 ID
 * - 1: userId     - 用户 ID
 * - 2: roomId     - 房间 ID（可选）
 * - 3: instanceType - 实例类型
 * - 4: fromInstanceId - 来源实例 ID
 * - 5: toInstanceId - 目标实例 ID
 * - 6: messageType - 消息类型
 */
object MessageHeaders {

    // ============================================================================
    // Header 键（数字索引）
    // ============================================================================

    /**
     * 会话 ID - 标识客户端与 Access 之间的连接会话
     */
    const val SESSION_ID = "0"

    /**
     * 用户 ID - 标识消息所属的用户
     */
    const val USER_ID = "1"

    /**
     * 房间 ID - 标识消息所属的房间
     * 可选：如果存在则按房间路由，否则按用户路由
     */
    const val ROOM_ID = "2"

    /**
     * 实例类型 - 标识消息来源实例类型
     */
    const val INSTANCE_TYPE = "3"

    /**
     * 来源实例 ID - 标识消息最初来自哪个实例
     */
    const val FROM_INSTANCE_ID = "4"

    /**
     * 去程实例 ID - 去往的实例
     */
    const val TO_INSTANCE_ID = "5"

    /**
     * 消息类型 - 标识消息的业务类型
     */
    const val MESSAGE_TYPE = "6"

    // ============================================================================
    // Header 值压缩（枚举值）
    // ============================================================================

    /**
     * 实例类型枚举值压缩
     */
    object InstanceType {
        const val ACCESS = "A"
        const val LOGIC = "L"
        const val BROKER = "B"
    }

    /**
     * 消息类型枚举值压缩
     */
    object MessageType {
        const val CLIENT = "C"
        const val SERVER = "S"
    }

    // ============================================================================
    // 路由推导辅助函数
    // ============================================================================

    /**
     * 从 headers 推导路由键
     * 规则：如果 roomId 存在则使用 roomId，否则使用 userId
     */
    fun getRouteKey(headers: Map<String, Any?>): String? {
        val roomId = headers[ROOM_ID] as? String
        val userId = headers[USER_ID] as? String
        return roomId ?: userId
    }

    /**
     * 判断是否按房间路由
     */
    fun isRoomRoute(headers: Map<String, Any?>): Boolean {
        return headers[ROOM_ID] != null
    }
}

