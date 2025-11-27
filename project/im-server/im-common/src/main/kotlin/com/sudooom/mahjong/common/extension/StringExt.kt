package com.sudooom.mahjong.common.extension

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.regex.Pattern

/**
 * 字符串扩展函数
 */

/**
 * 判断字符串是否为空或空白
 */
fun String?.isNullOrBlank(): Boolean = this == null || this.isBlank()

/**
 * 判断字符串是否非空
 */
fun String?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

/**
 * 时间戳转 LocalDateTime
 */
fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

/**
 * LocalDateTime 转时间戳
 */
fun LocalDateTime.toTimestamp(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

/**
 * 邮箱验证
 */
fun String.isEmail(): Boolean {
    val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return pattern.matcher(this).matches()
}

/**
 * 手机号验证（简单版）
 */
fun String.isPhone(): Boolean {
    val pattern = Pattern.compile("^1[3-9]\\d{9}$")
    return pattern.matcher(this).matches()
}

/**
 * 截取字符串
 */
fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.substring(0, maxLength - suffix.length) + suffix
    }
}
