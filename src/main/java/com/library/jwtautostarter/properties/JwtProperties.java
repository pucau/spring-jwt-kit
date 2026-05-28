package com.library.jwtautostarter.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for JWT authentication bound to the {@code jwt} prefix.
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private static final long DEFAULT_EXPIRATION_MS = 86_400_000L;

    /**
     * Base64-encoded secret key used to sign JWT tokens. Required.
     */
    @NotBlank(message = "jwt.secret-key must not be blank")
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
