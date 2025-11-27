package com.sudooom.mahjong.common.model

/**
 * 统一结果封装
 */
data class Result<T>(
    val code: Int,
    val message: String?,
    val data: T?,
    val success: Boolean
) {
    companion object {
        fun <T> success(data: T? = null, message: String? = "success"): Result<T> {
            return Result(0, message, data, true)
        }
        
        fun <T> failure(code: Int, message: String): Result<T> {
            return Result(code, message, null, false)
        }
        
        fun <T> failure(throwable: Throwable): Result<T> {
            return Result(500, throwable.message ?: "Unknown error", null, false)
        }
    }
}
