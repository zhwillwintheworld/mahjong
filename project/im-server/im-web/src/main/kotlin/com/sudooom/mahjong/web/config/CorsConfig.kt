package com.sudooom.mahjong.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/** CORS 配置 */
@Configuration
class CorsConfig {

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val config =
                CorsConfiguration().apply {
                    allowedOrigins = listOf("http://localhost:3000", "http://localhost:5173")
                    allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    allowedHeaders = listOf("*")
                    allowCredentials = true
                    maxAge = 3600
                }

        val source =
                UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", config) }

        return CorsWebFilter(source)
    }
}
