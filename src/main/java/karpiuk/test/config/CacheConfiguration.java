package karpiuk.test.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class CacheConfiguration {
    public final static String JWT_BLACK_LIST_CACHE_NAME = "jwt-black-list";
    public final static String EMAIL_CONFIRMATION_TOKEN_CACHE_NAME = "email-token-list";
    public final static String PASSWORD_RESET_TOKEN_CACHE_NAME = "reset-password-token-list";
    @Value("${spring.security.email-token.expiration-length}")
    private Long emailTokenExpirationLength;

    @Value("${spring.security.jwt.token.expire-length}")
    private Long jwtExpirationTimeLength;

    @Value("${spring.security.password-reset.token.length}")
    private Long resetPasswordTokenExpirationLength;

    @Value("${spring.security.redis.host-name}")
    private String hostName;

    @Value("${spring.security.redis.port.number}")
    private int portNumber;

    @Bean
    LettuceConnectionFactory jedisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(hostName, portNumber));
    }

    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> {
            Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
            configurationMap.put(JWT_BLACK_LIST_CACHE_NAME,
                    RedisCacheConfiguration.defaultCacheConfig()
                            .entryTtl(Duration.ofSeconds(jwtExpirationTimeLength)));


            configurationMap.put(EMAIL_CONFIRMATION_TOKEN_CACHE_NAME,
                    RedisCacheConfiguration.defaultCacheConfig()
                            .entryTtl(Duration.ofSeconds(emailTokenExpirationLength)));

            configurationMap.put(PASSWORD_RESET_TOKEN_CACHE_NAME,
                    RedisCacheConfiguration.defaultCacheConfig()
                            .entryTtl(Duration.ofSeconds(resetPasswordTokenExpirationLength)));

            builder.withInitialCacheConfigurations(configurationMap);
        };
    }
}
