package com.sudooom.mahjong.common.constant

/**
 * 系统常量
 */
object Constants {
    // Redis Key 前缀
    const val REDIS_USER_SESSION_PREFIX = "im:session:"
    const val REDIS_USER_ONLINE_PREFIX = "im:online:"
    const val REDIS_MESSAGE_CACHE_PREFIX = "im:msg:"
    
    // 默认值
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    
    // 超时时间（秒）
    const val HEARTBEAT_TIMEOUT = 60
    const val SESSION_TIMEOUT = 300
    
    // 消息长度限制
    const val MAX_MESSAGE_LENGTH = 5000
    const val MAX_USERNAME_LENGTH = 50
}
