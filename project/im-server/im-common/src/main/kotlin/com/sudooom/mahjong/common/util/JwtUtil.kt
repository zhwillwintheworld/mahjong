package com.sudooom.mahjong.common.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/** JWT 工具类 用于生成和验证 JWT Token */
object JwtUtil {

    // 默认过期时间：7 天（毫秒）
    private const val DEFAULT_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L

    /**
     * 生成密钥
     * @param secret 密钥字符串（至少 256 位 / 32 字节）
     */
    private fun generateKey(secret: String): SecretKey {
        val keyBytes = secret.toByteArray(StandardCharsets.UTF_8)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * 生成 JWT Token
     * @param userId 用户 ID
     * @param secret 密钥（至少 32 字符）
     * @param expirationTime 过期时间（毫秒），默认 7 天
     * @param claims 额外的声明
     */
    fun generateToken(
            userId: String,
            secret: String,
            expirationTime: Long = DEFAULT_EXPIRATION_TIME,
            claims: Map<String, Any> = emptyMap()
    ): String {
        val now = Date()
        val expiration = Date(now.time + expirationTime)

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiration)
                .claims(claims)
                .signWith(generateKey(secret))
                .compact()
    }

    /**
     * 验证并解析 JWT Token
     * @param token JWT Token
     * @param secret 密钥
     * @return Claims 对象，包含所有声明
     * @throws io.jsonwebtoken.JwtException 如果 token 无效或过期
     */
    fun parseToken(token: String, secret: String): Claims {
        return Jwts.parser()
                .verifyWith(generateKey(secret))
                .build()
                .parseSignedClaims(token)
                .payload
    }

    /**
     * 从 Token 中获取用户 ID
     * @param token JWT Token
     * @param secret 密钥
     * @return 用户 ID
     */
    fun getUserId(token: String, secret: String): String {
        return parseToken(token, secret).subject
    }

    /**
     * 验证 Token 是否有效
     * @param token JWT Token
     * @param secret 密钥
     * @return true 如果有效，false 如果无效或过期
     */
    fun validateToken(token: String, secret: String): Boolean {
        return try {
            parseToken(token, secret)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查 Token 是否过期
     * @param token JWT Token
     * @param secret 密钥
     * @return true 如果已过期
     */
    fun isTokenExpired(token: String, secret: String): Boolean {
        return try {
            val claims = parseToken(token, secret)
            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }
}
