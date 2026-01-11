package com.fulfai.sellingpartner.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.quarkus.logging.Log;

public final class ApprovalTokenUtil {

    private static final String HMAC_ALGO = "HmacSHA256";

    // TODO: move to application.properties / vault in prod
    private static final String SECRET =
            "CHANGE_ME_TO_LONG_RANDOM_SECRET";

    // 24 hours (seconds)
    private static final long DEFAULT_EXPIRY_SECONDS = 24 * 60 * 60;

    private ApprovalTokenUtil() {}

    /* =========================
       CREATE TOKEN
    ========================== */

    public static String generateToken(
            String companyId,
            String requestId
    ) {

        long expiresAt =
                Instant.now().getEpochSecond() + DEFAULT_EXPIRY_SECONDS;

        String payload =
                companyId + "|" + requestId + "|" + expiresAt;

        String signature = sign(payload);

        String token = payload + "|" + signature;

        // URL-safe Base64 (required for query params)
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        token.getBytes(StandardCharsets.UTF_8)
                );
    }

    /* =========================
       VALIDATE TOKEN
    ========================== */

    public static TokenData validateToken(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Invalid or expired approval token");
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(token),
                    StandardCharsets.UTF_8
            );

            // companyId | requestId | expiresAt | signature
            String[] parts = decoded.split("\\|", 4);
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid approval token format");
            }

            String companyId = parts[0];
            String requestId = parts[1];
            long expiresAt = Long.parseLong(parts[2]);
            String signature = parts[3];

            if (Instant.now().getEpochSecond() > expiresAt) {
                throw new IllegalArgumentException("Approval token expired");
            }

            String payload =
                    companyId + "|" + requestId + "|" + expiresAt;

            String expectedSignature = sign(payload);

            if (!constantTimeEquals(signature, expectedSignature)) {
                throw new IllegalArgumentException("Invalid approval token signature");
            }

            Log.debugf(
                    "Approval token valid (companyId=%s, requestId=%s)",
                    companyId,
                    requestId
            );

            return new TokenData(companyId, requestId);

        } catch (IllegalArgumentException e) {
            // propagate meaningful token errors
            throw e;
        } catch (Exception e) {
            // everything else → invalid token
            throw new IllegalArgumentException(
                    "Invalid or expired approval token",
                    e
            );
        }
    }

    /* =========================
       HELPERS
    ========================== */

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(
                    new SecretKeySpec(
                            SECRET.getBytes(StandardCharsets.UTF_8),
                            HMAC_ALGO
                    )
            );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            mac.doFinal(
                                    data.getBytes(StandardCharsets.UTF_8)
                            )
                    );

        } catch (Exception e) {
            throw new RuntimeException("Failed to sign approval token", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /* =========================
       TOKEN DATA
    ========================== */

    public static final class TokenData {

        private final String companyId;
        private final String requestId;

        public TokenData(String companyId, String requestId) {
            this.companyId = companyId;
            this.requestId = requestId;
        }

        public String getCompanyId() {
            return companyId;
        }

        public String getRequestId() {
            return requestId;
        }
    }
}
