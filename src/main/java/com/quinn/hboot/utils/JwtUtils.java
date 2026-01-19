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

    public static String createToken(String tokenId) {
        return Jwts.builder()
                .claim("tokenId", tokenId)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }

    public static String parseTokenId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("tokenId", String.class);
    }

    /*public static String createToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }*/

    public static String parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

      /* 解析 Token
     */
    public static Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 判断是否过期
     */
    public static boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
}
