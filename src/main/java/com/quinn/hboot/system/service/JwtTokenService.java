package com.quinn.hboot.system.service;

/**
 * JWT 令牌服务。
 */
public interface JwtTokenService {

    /**
     * 创建令牌。
     */
    String createToken(String tokenId);

    /**
     * 解析令牌中的 tokenId。
     */
    String parseTokenId(String token);
}
