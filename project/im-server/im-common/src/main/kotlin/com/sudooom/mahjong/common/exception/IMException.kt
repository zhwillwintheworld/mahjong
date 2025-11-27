package com.sudooom.mahjong.common.exception

/**
 * 错误码
 */
enum class ErrorCode(val code: Int, val message: String) {
    // 通用错误 1xxx
    SUCCESS(0, "成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1004, "资源不存在"),
    
    // 认证错误 2xxx
    UNAUTHORIZED(2001, "未授权"),
    TOKEN_EXPIRED(2002, "Token 已过期"),
    TOKEN_INVALID(2003, "Token 无效"),
    
    // 业务错误 3xxx
    USER_NOT_FOUND(3001, "用户不存在"),
    USER_OFFLINE(3002, "用户不在线"),
    MESSAGE_TOO_LONG(3003, "消息过长"),
    GROUP_NOT_FOUND(3004, "群组不存在"),
    
    // 连接错误 4xxx
    CONNECTION_FAILED(4001, "连接失败"),
    CONNECTION_CLOSED(4002, "连接已关闭"),
    HEARTBEAT_TIMEOUT(4003, "心跳超时");
}

/**
 * IM 自定义异常
 */
open class IMException(
    val errorCode: ErrorCode,
    message: String? = errorCode.message,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    val code: Int
        get() = errorCode.code
}

/**
 * 参数错误异常
 */
class ParamException(
    message: String? = null,
    cause: Throwable? = null
) : IMException(ErrorCode.PARAM_ERROR, message, cause)

/**
 * 未授权异常
 */
class UnauthorizedException(
    message: String? = null,
    cause: Throwable? = null
) : IMException(ErrorCode.UNAUTHORIZED, message, cause)

/**
 * 业务异常
 */
class BusinessException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : IMException(errorCode, message, cause)
