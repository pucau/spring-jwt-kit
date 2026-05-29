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

    /**
     * Additional ant-style path patterns appended to {@link #excludedPaths}. Lets consuming
     * apps extend the exclusion list without replacing the defaults.
     */
    private List<String> additionalExcludedPaths = new ArrayList<>();

    /**
     * Optional issuer embedded as the {@code iss} claim and validated on parse.
     */
    private String issuer;

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

    public List<String> getAdditionalExcludedPaths() {
        return additionalExcludedPaths;
    }

    public void setAdditionalExcludedPaths(List<String> additionalExcludedPaths) {
        this.additionalExcludedPaths = additionalExcludedPaths;
    }

    public List<String> getMergedExcludedPaths() {
        List<String> merged = new ArrayList<>(excludedPaths);
        merged.addAll(additionalExcludedPaths);
        return merged;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
