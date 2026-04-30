package com.quinn.hboot.system.service.impl;

import com.quinn.hboot.config.properties.AuthProperties;
import com.quinn.hboot.system.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌服务实现。
 */
@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final AuthProperties authProperties;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] secretBytes = authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT 密钥长度不足，至少需要 32 字节");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
    }

    @Override
    public String createToken(String tokenId) {
        long expireMillis = authProperties.getJwtExpireMinutes() * 60 * 1000;
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireMillis);

        return Jwts.builder()
                .claim("tokenId", tokenId)
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String parseTokenId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("tokenId", String.class);
    }
}
