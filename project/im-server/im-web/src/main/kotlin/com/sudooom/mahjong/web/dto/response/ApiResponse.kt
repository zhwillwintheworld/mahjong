package com.sudooom.mahjong.web.dto.response

/** API 统一响应 */
data class ApiResponse<T>(
        val code: Int,
        val message: String,
        val data: T? = null,
        val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(data: T? = null, message: String = "success"): ApiResponse<T> {
            return ApiResponse(0, message, data)
        }

        fun <T> error(code: Int = 500, message: String = "error"): ApiResponse<T> {
            return ApiResponse(code, message, null)
        }
    }
}
