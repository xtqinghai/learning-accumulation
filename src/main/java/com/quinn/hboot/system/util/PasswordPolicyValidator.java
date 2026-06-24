package com.quinn.hboot.system.util;

import com.quinn.hboot.common.exception.BusinessException;

import java.util.regex.Pattern;

public final class PasswordPolicyValidator {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[^A-Za-z0-9]");

    private PasswordPolicyValidator() {
    }

    public static void validateOrThrow(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("密码不能为空");
        }
        if (password.length() < 8) {
            throw new BusinessException("密码长度不能少于8位");
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new BusinessException("密码必须包含至少一个大写字母");
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new BusinessException("密码必须包含至少一个小写字母");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new BusinessException("密码必须包含至少一个数字");
        }
        if (!SPECIAL_PATTERN.matcher(password).find()) {
            throw new BusinessException("密码必须包含至少一个特殊字符");
        }
    }
}
