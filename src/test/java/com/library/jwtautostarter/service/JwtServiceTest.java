package com.library.jwtautostarter.service;

import com.library.jwtautostarter.properties.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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

    @Test
    void generateTokenEmbeddsIssuerClaimWhenConfigured() {
        JwtProperties props = new JwtProperties();
        props.setSecretKey(SECRET_KEY);
        props.setExpirationMs(EXPIRATION_MS);
        props.setIssuer("my-app");
        JwtService serviceWithIssuer = new JwtService(props);

        String token = serviceWithIssuer.generateToken(userDetails);

        String issuer = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getIssuer();

        assertThat(issuer).isEqualTo("my-app");
    }

    @Test
    void issuerRoundtripPassesValidation() {
        JwtProperties props = new JwtProperties();
        props.setSecretKey(SECRET_KEY);
        props.setExpirationMs(EXPIRATION_MS);
        props.setIssuer("my-app");
        JwtService serviceWithIssuer = new JwtService(props);

        String token = serviceWithIssuer.generateToken(userDetails);

        assertThat(serviceWithIssuer.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void issuerMismatchThrowsJwtException() {
        JwtProperties propsA = new JwtProperties();
        propsA.setSecretKey(SECRET_KEY);
        propsA.setExpirationMs(EXPIRATION_MS);
        propsA.setIssuer("issuer-a");
        JwtService serviceA = new JwtService(propsA);

        JwtProperties propsB = new JwtProperties();
        propsB.setSecretKey(SECRET_KEY);
        propsB.setExpirationMs(EXPIRATION_MS);
        propsB.setIssuer("issuer-b");
        JwtService serviceB = new JwtService(propsB);

        String token = serviceA.generateToken(userDetails);

        assertThatThrownBy(() -> serviceB.extractUsername(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void missingIssuerClaimFailsWhenIssuerRequired() {
        String tokenWithoutIssuer = jwtService.generateToken(userDetails);

        JwtProperties propsWithIssuer = new JwtProperties();
        propsWithIssuer.setSecretKey(SECRET_KEY);
        propsWithIssuer.setExpirationMs(EXPIRATION_MS);
        propsWithIssuer.setIssuer("my-app");
        JwtService serviceWithIssuer = new JwtService(propsWithIssuer);

        assertThatThrownBy(() -> serviceWithIssuer.extractUsername(tokenWithoutIssuer))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void noIssuerConfiguredTokenHasNoIssClaim() {
        String token = jwtService.generateToken(userDetails);

        String issuer = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getIssuer();

        assertThat(issuer).isNull();
    }
}
