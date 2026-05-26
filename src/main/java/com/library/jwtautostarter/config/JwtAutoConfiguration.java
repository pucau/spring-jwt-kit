package com.library.jwtautostarter.config;

import com.library.jwtautostarter.exception.GlobalExceptionHandler;
import com.library.jwtautostarter.filter.JwtAuthenticationFilter;
import com.library.jwtautostarter.properties.JwtProperties;
import com.library.jwtautostarter.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;

/**
 * Spring Boot auto-configuration that registers all JWT starter beans when
 * {@code jwt.secret-key} is present in the application properties.
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "jwt", name = "secret-key")
public class JwtAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtAutoConfiguration.class);

    /**
     * Registers {@link JwtService} unless the consuming application provides its own.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(JwtProperties properties) {
        Assert.hasText(properties.getSecretKey(),
                "jwt.secret-key must not be blank — add it to your application properties");
        log.info("Registering JwtService with expiration={}ms", properties.getExpirationMs());
        return new JwtService(properties);
    }

    /**
     * Registers {@link JwtAuthenticationFilter} unless the consuming application provides its own.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
                                                           UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    /**
     * Registers {@link JwtSecurityConfiguration} unless the consuming application provides its own.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtSecurityConfiguration jwtSecurityConfiguration(JwtAuthenticationFilter filter,
                                                              JwtProperties properties) {
        return new JwtSecurityConfiguration(filter, properties);
    }

    /**
     * Registers the {@link SecurityFilterChain} unless the consuming application provides its own.
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtSecurityConfiguration config) throws Exception {
        return config.securityFilterChain(http);
    }

    /**
     * Registers {@link GlobalExceptionHandler} unless the consuming application provides its own.
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
