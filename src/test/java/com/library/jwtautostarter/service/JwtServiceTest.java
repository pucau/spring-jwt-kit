package com.library.jwtautostarter.service;

import com.library.jwtautostarter.properties.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET_KEY =
            "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2LWFsZ29yaXRobQ==";
    private static final long EXPIRATION_MS = 86_400_000L;

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecretKey(SECRET_KEY);
        properties.setExpirationMs(EXPIRATION_MS);

        jwtService = new JwtService(properties);

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateTokenReturnsNonNullToken() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsernameReturnsCorrectSubject() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void isTokenValidReturnsTrueForValidToken() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValidReturnsFalseForWrongUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = User.builder()
                .username("otheruser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseForExpiredToken() {
        JwtProperties shortLivedProps = new JwtProperties();
        shortLivedProps.setSecretKey(SECRET_KEY);
        shortLivedProps.setExpirationMs(1L);

        JwtService shortLivedService = new JwtService(shortLivedProps);
        String token = shortLivedService.generateToken(userDetails);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> shortLivedService.isTokenValid(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void generateTokenProducesDifferentTokensOnSubsequentCalls() {
        String token1 = jwtService.generateToken(userDetails);
        String token2 = jwtService.generateToken(userDetails);
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractUsernameThrowsForTamperedToken() {
        String token = jwtService.generateToken(userDetails);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtService.extractUsername(tamperedToken))
                .isInstanceOf(Exception.class);
    }
}
