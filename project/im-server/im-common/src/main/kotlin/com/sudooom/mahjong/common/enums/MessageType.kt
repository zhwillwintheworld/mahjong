package com.sudooom.mahjong.common.enums

/**
 * 消息类型枚举
 */
enum class MessageType(val code: Int, val desc: String) {
    TEXT(0, "文本消息"),
    IMAGE(1, "图片消息"),
    FILE(2, "文件消息"),
    VOICE(3, "语音消息"),
    VIDEO(4, "视频消息"),
    SYSTEM(5, "系统消息"),
    HEARTBEAT(6, "心跳消息");
    
    companion object {
        fun fromCode(code: Int): MessageType? {
            return entries.find { it.code == code }
        }
    }
}

/**
 * 用户状态枚举
 */
enum class UserStatus(val code: Int, val desc: String) {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    BUSY(2, "忙碌"),
    AWAY(3, "离开");
    
    companion object {
        fun fromCode(code: Int): UserStatus? {
            return entries.find { it.code == code }
        }
    }
}
