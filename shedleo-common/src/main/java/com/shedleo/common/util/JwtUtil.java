package com.shedleo.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtUtil {

    private static final String SECRET = "shedleo-demo-secret-key";
    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    public static String generateToken(Long userId) {
        String payload = userId + "." + (System.currentTimeMillis() + EXPIRATION_MS);
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public static Long getUserId(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\.");
            if (parts.length != 2) {
                return null;
            }
            long expTime = Long.parseLong(parts[1]);
            if (System.currentTimeMillis() > expTime) {
                return null;
            }
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            return null;
        }
    }
}
