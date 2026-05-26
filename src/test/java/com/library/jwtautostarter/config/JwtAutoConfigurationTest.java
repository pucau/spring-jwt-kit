package com.library.jwtautostarter.config;

import com.library.jwtautostarter.exception.GlobalExceptionHandler;
import com.library.jwtautostarter.filter.JwtAuthenticationFilter;
import com.library.jwtautostarter.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAutoConfigurationTest {

    private static final String VALID_SECRET =
            "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2LWFsZ29yaXRobQ==";

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    WebMvcAutoConfiguration.class,
                    SecurityAutoConfiguration.class,
                    JwtAutoConfiguration.class))
            .withBean(UserDetailsService.class, () -> mock(UserDetailsService.class));

    @Test
    void autoConfigurationRegistersJwtServiceWhenSecretKeyPresent() {
        contextRunner
                .withPropertyValues("jwt.secret-key=" + VALID_SECRET)
                .run(ctx -> assertThat(ctx).hasSingleBean(JwtService.class));
    }

    @Test
    void autoConfigurationRegistersJwtFilterWhenSecretKeyPresent() {
        contextRunner
                .withPropertyValues("jwt.secret-key=" + VALID_SECRET)
                .run(ctx -> assertThat(ctx).hasSingleBean(JwtAuthenticationFilter.class));
    }

    @Test
    void autoConfigurationRegistersGlobalExceptionHandlerWhenSecretKeyPresent() {
        contextRunner
                .withPropertyValues("jwt.secret-key=" + VALID_SECRET)
                .run(ctx -> assertThat(ctx).hasSingleBean(GlobalExceptionHandler.class));
    }

    @Test
    void autoConfigurationDoesNotRegisterBeansWhenSecretKeyAbsent() {
        contextRunner
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(JwtService.class);
                    assertThat(ctx).doesNotHaveBean(JwtAuthenticationFilter.class);
                });
    }

    @Test
    void customJwtServiceNotOverriddenByAutoConfiguration() {
        JwtService customService = mock(JwtService.class);

        contextRunner
                .withPropertyValues("jwt.secret-key=" + VALID_SECRET)
                .withBean(JwtService.class, () -> customService)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(JwtService.class);
                    assertThat(ctx.getBean(JwtService.class)).isSameAs(customService);
                });
    }

    @Test
    void expirationMsDefaultIs86400000() {
        contextRunner
                .withPropertyValues("jwt.secret-key=" + VALID_SECRET)
                .run(ctx -> {
                    var props = ctx.getBean(com.library.jwtautostarter.properties.JwtProperties.class);
                    assertThat(props.getExpirationMs()).isEqualTo(86_400_000L);
                });
    }

    @Test
    void customExpirationMsIsApplied() {
        contextRunner
                .withPropertyValues(
                        "jwt.secret-key=" + VALID_SECRET,
                        "jwt.expiration-ms=3600000")
                .run(ctx -> {
                    var props = ctx.getBean(com.library.jwtautostarter.properties.JwtProperties.class);
                    assertThat(props.getExpirationMs()).isEqualTo(3_600_000L);
                });
    }
}
