package com.sudooom.mahjong.common.extension

import com.sudooom.mahjong.common.annotation.Loggable
import com.sudooom.mahjong.common.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/** Flow 扩展对象，实现 Loggable 接口以使用 logger */
private object FlowExtLogger : Loggable

/** 将 Flow 转换为 Result */
fun <T> Flow<T>.toResult(): Flow<Result<T>> {
    return this.map { Result.success(it) }.catch { emit(Result.failure(it)) }
}

/** Flow 错误处理 */
fun <T> Flow<T>.catchAndLog(message: String): Flow<T> {
    return this.catch { e ->
        FlowExtLogger.logger.error("$message: ${e.message}", e)
        throw e
    }
}

/** Flow 日志记录 */
fun <T> Flow<T>.logEach(message: (T) -> String): Flow<T> {
    return this.onEach { FlowExtLogger.logger.info(message(it)) }
}
