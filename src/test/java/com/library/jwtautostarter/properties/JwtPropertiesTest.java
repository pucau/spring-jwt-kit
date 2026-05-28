package com.library.jwtautostarter.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(JwtPropertiesConfig.class);

    @Configuration
    @EnableConfigurationProperties(JwtProperties.class)
    static class JwtPropertiesConfig {}

    @Test
    void blankSecretKeyFailsValidation() {
        contextRunner
                .withPropertyValues("jwt.secret-key= ")
                .run(ctx -> assertThat(ctx).hasFailed());
    }

    @Test
    void emptySecretKeyFailsValidation() {
        contextRunner
                .withPropertyValues("jwt.secret-key=")
                .run(ctx -> assertThat(ctx).hasFailed());
    }

    @Test
    void validSecretKeyBindsSuccessfully() {
        contextRunner
                .withPropertyValues("jwt.secret-key=dGVzdC1zZWNyZXQta2V5")
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx.getBean(JwtProperties.class).getSecretKey())
                            .isEqualTo("dGVzdC1zZWNyZXQta2V5");
                });
    }

    @Test
    void defaultExpirationMsIs86400000() {
        JwtProperties props = new JwtProperties();
        assertThat(props.getExpirationMs()).isEqualTo(86_400_000L);
    }

    @Test
    void defaultExcludedPathsContainsAuthAndPublic() {
        JwtProperties props = new JwtProperties();
        assertThat(props.getExcludedPaths()).containsExactlyInAnyOrder("/auth/**", "/public/**");
    }

    @Test
    void secretKeyIsNullByDefault() {
        JwtProperties props = new JwtProperties();
        assertThat(props.getSecretKey()).isNull();
    }

    @Test
    void settersAndGettersWorkCorrectly() {
        JwtProperties props = new JwtProperties();
        props.setSecretKey("my-secret");
        props.setExpirationMs(3600000L);
        props.setExcludedPaths(List.of("/open/**"));

        assertThat(props.getSecretKey()).isEqualTo("my-secret");
        assertThat(props.getExpirationMs()).isEqualTo(3600000L);
        assertThat(props.getExcludedPaths()).containsExactly("/open/**");
    }
}
