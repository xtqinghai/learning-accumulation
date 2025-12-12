package com.quinn.hboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

/**
 * @author yu-qh
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端域名（本地开发一般是 http://localhost:5173 或 8080）
//        config.addAllowedOrigin("http://localhost:5173");
        //1.允许任何来源
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        // 允许携带 Cookie/Token
        config.setAllowCredentials(true);
        // 允许所有请求方法（GET/POST/PUT/DELETE 等）
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 暴露自定义响应头（如 Token 相关）
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有接口生效
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

