package com.sudooom.mahjong.common.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/** JWT 工具类 用于生成和验证 JWT Token */
@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val jwtSecret: String
) {

    // 默认过期时间：7 天（毫秒）
    private val defaultExpirationTime = 7 * 24 * 60 * 60 * 1000L

    // 缓存的 SecretKey，在构造时生成
    private val secretKey: SecretKey by lazy {
        val keyBytes = jwtSecret.toByteArray(StandardCharsets.UTF_8)
        Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * 生成 JWT Token
     * @param userId 用户 ID
     * @param expirationTime 过期时间（毫秒），默认 7 天
     * @param claims 额外的声明
     */
    fun generateToken(
        userId: String,
        expirationTime: Long = defaultExpirationTime,
        claims: Map<String, Any> = emptyMap()
    ): String {
        val now = Date()
        val expiration = Date(now.time + expirationTime)

        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(expiration)
            .claims(claims)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 验证并解析 JWT Token
     * @param token JWT Token
     * @return Claims 对象，包含所有声明
     * @throws io.jsonwebtoken.JwtException 如果 token 无效或过期
     */
    fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * 从 Token 中获取用户 ID
     * @param token JWT Token
     * @return 用户 ID
     */
    fun getUserId(token: String): String {
        return parseToken(token).subject
    }

    /**
     * 验证 Token 是否有效
     * @param token JWT Token
     * @return true 如果有效，false 如果无效或过期
     */
    fun validateToken(token: String): Boolean {
        return try {
            parseToken(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查 Token 是否过期
     * @param token JWT Token
     * @return true 如果已过期
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = parseToken(token)
            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }
}
