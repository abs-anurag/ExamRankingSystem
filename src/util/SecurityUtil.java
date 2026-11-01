package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SecurityUtil provides SHA-256 password hashing and validation.
 */
public class SecurityUtil {

    /**
     * Computes the hex-encoded SHA-256 hash of a plaintext password.
     *
     * @param password plaintext password
     * @return 64-character hexadecimal SHA-256 hash string
     */
    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies whether a plaintext password matches a stored SHA-256 hash.
     */
    public static boolean verifyPassword(String plaintext, String storedHash) {
        if (plaintext == null || storedHash == null) {
            return false;
        }
        String calculated = hashPassword(plaintext);
        return calculated.equalsIgnoreCase(storedHash);
    }
}

