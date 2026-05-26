package com.library.jwtautostarter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for JWT authentication bound to the {@code jwt} prefix.
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private static final long DEFAULT_EXPIRATION_MS = 86_400_000L;

    /**
     * Base64-encoded secret key used to sign JWT tokens. Required.
     */
    private String secretKey;

    /**
     * Token validity duration in milliseconds. Defaults to 24 hours.
     */
    private long expirationMs = DEFAULT_EXPIRATION_MS;

    /**
     * Ant-style path patterns that bypass JWT authentication.
     */
    private List<String> excludedPaths = new ArrayList<>(List.of("/auth/**", "/public/**"));

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }
}
