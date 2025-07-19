package io.github.nekosora.nlink.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PasswordUtils {
    /**
     * 计算字符串的SHA-256哈希值
     * @param input 原始字符串
     * @return 十六进制格式的哈希值（小写）
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 验证密码是否匹配
     * @param inputPassword 用户输入的密码（明文）
     * @param storedHash 存储的哈希密码
     */
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        return sha256(inputPassword).equals(storedHash);
    }
}