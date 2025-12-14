package com.example.moviediary.security;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordUtils {

    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static String hashPassword(String password, String saltBase64) {
        try {
            byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hashed, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    public static boolean verifyPassword(String inputPassword, String saltBase64, String expectedHash) {
        String inputHash = hashPassword(inputPassword, saltBase64);
        return inputHash.equals(expectedHash);
    }
}
