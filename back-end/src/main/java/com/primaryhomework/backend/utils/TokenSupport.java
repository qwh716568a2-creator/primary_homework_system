package com.primaryhomework.backend.utils;

import com.primaryhomework.backend.entity.po.UserPo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public final class TokenSupport {

    private static final String TOKEN_PREFIX = "phs";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String DEFAULT_SECRET = "primary-homework-local-secret-key-please-change";

    private static String secret = DEFAULT_SECRET;
    private static long expireSeconds = 7200L;

    public TokenSupport(
            @Value("${app.jwt.secret:" + DEFAULT_SECRET + "}") String secret,
            @Value("${app.jwt.expire-seconds:7200}") Long expireSeconds
    ) {
        TokenSupport.secret = StringUtils.hasText(secret) ? secret.trim() : DEFAULT_SECRET;
        TokenSupport.expireSeconds = expireSeconds == null || expireSeconds <= 0 ? 7200L : expireSeconds;
    }

    public static String buildToken(UserPo user) {
        long expireAt = Instant.now().getEpochSecond() + expireSeconds;
        return buildToken(user.getId(), user.getRoleType(), expireAt);
    }

    public static ParsedToken parseAuthorization(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7).trim() : authorization.trim();
        return parseToken(token);
    }

    public static ParsedToken parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String[] parts = token.trim().split("\\.");
        if (parts.length != 3 || !TOKEN_PREFIX.equals(parts[0])) {
            return null;
        }
        String payload = parts[1];
        String signature = parts[2];
        if (!secureEquals(signature, sign(payload))) {
            return null;
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            String[] payloadParts = decoded.split("\\|", -1);
            if (payloadParts.length != 3) {
                return null;
            }
            Long userId = Long.parseLong(payloadParts[0]);
            String roleType = payloadParts[1];
            long expireAt = Long.parseLong(payloadParts[2]);
            if (!StringUtils.hasText(roleType) || expireAt <= Instant.now().getEpochSecond()) {
                return null;
            }
            return new ParsedToken(userId, roleType, expireAt);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static String buildToken(Long userId, String roleType, long expireAt) {
        String payloadText = userId + "|" + roleType + "|" + expireAt;
        String payload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payloadText.getBytes(StandardCharsets.UTF_8));
        return TOKEN_PREFIX + "." + payload + "." + sign(payload);
    }

    private static String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to sign token", e);
        }
    }

    private static boolean secureEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.US_ASCII),
                right.getBytes(StandardCharsets.US_ASCII)
        );
    }

    public record ParsedToken(Long userId, String roleType, Long expireAt) {
    }
}
