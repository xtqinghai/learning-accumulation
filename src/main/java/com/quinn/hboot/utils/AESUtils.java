package com.quinn.hboot.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 解密工具。
 * <p>
 * 当前主要用于兼容前端登录密码的 AES/ECB/PKCS5Padding 加密方式。
 * 后续若接入更完善的认证网关或密钥管理，应优先迁移配置方式，
 * 避免把密钥长期固定在代码中。
 */
public final class AESUtils {

    /**
     * 当前前后端约定的 AES 密钥。
     * 生产环境建议改为配置中心或环境变量注入。
     */
    private static final String AES_KEY = "438704315@qq.com";

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_MODE = "AES/ECB/PKCS5Padding";

    private AESUtils() {
    }

    /**
     * 解密前端提交的登录密码。
     *
     * @param encryptedPwd AES 加密后的 Base64 字符串
     * @return 明文密码
     */
    public static String decrypt(String encryptedPwd) {
        try {
            // 1. 创建AES密钥
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            // 2. 初始化解密器
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            // 3. Base64解码 → AES解密 → 明文
            byte[] decryptBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPwd));
            return new String(decryptBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("密码解密失败", ex);
        }
    }
}
