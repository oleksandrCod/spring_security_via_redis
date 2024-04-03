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
    public final static String BLACKLIST_CACHE_NAME = "jwt-black-list";
    @Value("${security.jwt.token.expire-length}")
    private Long jwtExpirationTimeLength;
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
            configurationMap.put(BLACKLIST_CACHE_NAME, RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofSeconds(jwtExpirationTimeLength)));
            builder.withInitialCacheConfigurations(configurationMap);
        };
    }
}
