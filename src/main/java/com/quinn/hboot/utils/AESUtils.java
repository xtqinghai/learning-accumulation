package com.quinn.hboot.utils;

/**
* @program: h-boot
* @ClassName AESUtils
* @description: 说明：AES加密解密工具类（ECB模式，和前端crypto-js对齐）
* @author: yuqh
* @create: 2025-12-12 22:04
* @Version 1.0
**/
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESUtils {
    // 前后端一致的密钥（16位），生产环境建议配置在nacos/yml中
    private static final String AES_KEY = "438704315@qq.com";
    private static final String ALGORITHM = "AES";
    // 算法/模式/填充（必须和前端一致）
    private static final String CIPHER_MODE = "AES/ECB/PKCS5Padding";

    /**
     * AES解密（前端加密后的字符串 → 明文密码）
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
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败", e);
        }
    }
}
