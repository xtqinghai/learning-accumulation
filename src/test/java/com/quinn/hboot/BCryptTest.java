package com.quinn.hboot;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt密码加密测试
 * 前端密码加密传输
 * @author quinn
 * @date 2021/06/01
 */
public class BCryptTest {
    public static void main(String[] args) {
        // 重新生成密码并测试
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String newEncodedPassword = encoder.encode(rawPassword);

        System.out.println("新生成的密码: " + newEncodedPassword);
        boolean matches = encoder.matches(rawPassword, newEncodedPassword);
        System.out.println("新密码匹配结果: " + matches); // 应该输出 true
    }
}
