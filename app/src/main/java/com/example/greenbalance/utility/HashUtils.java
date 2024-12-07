package com.example.greenbalance.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Base64;

public class HashUtils {

    /**
     * Generates a SHA-256 hash for the given input string.
     *
     * @param input The input string to hash
     * @return The hashed string encoded in Base64
     */
    public static String generateHash(String input) {
        try {
            // Create SHA-256 MessageDigest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Perform the hash
            byte[] hashBytes = digest.digest(input.getBytes());

            // Encode the hash in Base64 for compatibility with JavaScript
            return Base64.encodeToString(hashBytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
