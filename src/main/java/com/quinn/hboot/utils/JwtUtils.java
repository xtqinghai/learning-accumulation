package com.quinn.hboot.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

/**
 * JWT 工具类
 *
 * @author yu-qh
 */
public class JwtUtils {

    private static final String SECRET = "yuqinghai-hboot-438704315@qq.com"; // 至少 32 字节
    private static final long EXPIRE = 1000 * 60 * 60 * 24; // 1天

    public static String createToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }

    public static String parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
