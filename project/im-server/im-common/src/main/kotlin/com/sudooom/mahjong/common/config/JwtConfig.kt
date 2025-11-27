package com.sudooom.mahjong.common.config

/** JWT 配置 应用层应该提供具体的配置值 */
data class JwtConfig(
        /** JWT 密钥（建议至少 32 字符） 应该从环境变量或配置文件中读取 */
        val secret: String,

        /** Token 过期时间（毫秒） 默认 7 天 */
        val expirationTime: Long = 7 * 24 * 60 * 60 * 1000L,

        /** Token 前缀（例如 "Bearer "） */
        val tokenPrefix: String = "Bearer ",

        /** Token Header 名称 */
        val headerName: String = "Authorization"
)
