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
    @field:Value(value = $$"${jwt.secret}") private val jwtSecret: String
) {

    // 默认过期时间：7 天（毫秒）
    private val defaultExpirationTime = 7 * 24 * 60 * 60 * 1000L
    
    // Token 标识符，用于 subject
    private val tokenSubject = "sudooom"
    
    // Claims 键常量
    companion object {
        const val CLAIM_USER_ID = "userId"
        const val CLAIM_DEVICE_ID = "deviceId"
    }

    // 缓存的 SecretKey，在构造时生成
    private val secretKey: SecretKey by lazy {
        val keyBytes = jwtSecret.toByteArray(StandardCharsets.UTF_8)
        Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * 生成 JWT Token
     * @param userId 用户 ID
     * @param deviceId 设备 ID（可选）
     * @param expirationTime 过期时间（毫秒），默认 7 天
     * @param additionalClaims 额外的声明
     */
    fun generateToken(
        userId: String,
        deviceId: String? = null,
        expirationTime: Long = defaultExpirationTime,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String {
        val now = Date()
        val expiration = Date(now.time + expirationTime)
        
        val claims = mutableMapOf<String, Any>(
            CLAIM_USER_ID to userId
        )
        
        deviceId?.let { claims[CLAIM_DEVICE_ID] = it }
        claims.putAll(additionalClaims)

        return Jwts.builder()
            .subject(tokenSubject)
            .issuedAt(now)
            .expiration(expiration)
            .claims(claims)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 验证并解析 JWT Token
     * 会在解析前验证：
     * 1. Token 签名是否有效
     * 2. Token 是否过期
     * 3. Subject 是否为 "sudooom"
     * @param token JWT Token
     * @return Claims 对象，包含所有声明
     * @throws io.jsonwebtoken.JwtException 如果 token 无效或过期
     * @throws io.jsonwebtoken.ExpiredJwtException 如果 token 已过期
     * @throws io.jsonwebtoken.security.SignatureException 如果签名验证失败
     * @throws io.jsonwebtoken.MalformedJwtException 如果 subject 不匹配
     */
    fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .requireSubject(tokenSubject)  // 验证 subject 必须为 "sudooom"
            .build()
            .parseSignedClaims(token)  // 自动验证过期时间
            .payload
    }

    /**
     * 从 Token 中获取用户 ID
     * @param token JWT Token
     * @return 用户 ID
     */
    fun getUserId(token: String): String {
        try{
            return parseToken(token).get(CLAIM_USER_ID, String::class.java)
        } catch (_: Exception) {
            throw IllegalArgumentException("Invalid token")
        }
    }
    
    /**
     * 从 Token 中获取设备 ID
     * @param token JWT Token
     * @return 设备 ID，如果不存在则返回 null
     */
    fun getDeviceId(token: String): String? {
        return parseToken(token).get(CLAIM_DEVICE_ID, String::class.java)
    }
    
    /**
     * 从 Token 中获取自定义 claim
     * @param token JWT Token
     * @param claimKey claim 的键
     * @return claim 的值
     */
    fun <T> getClaim(token: String, claimKey: String, requiredType: Class<T>): T? {
        return parseToken(token).get(claimKey, requiredType)
    }

    /**
     * 验证 Token 是否有效
     * 会验证签名、过期时间和 subject
     * @param token JWT Token
     * @return true 如果有效，false 如果无效或过期
     */
    fun validateToken(token: String): Boolean {
        return try {
            parseToken(token)  // parseToken 已包含所有必要的验证
            true
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            true
        }
    }
}

