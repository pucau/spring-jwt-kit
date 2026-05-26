package com.library.jwtautostarter.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

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
