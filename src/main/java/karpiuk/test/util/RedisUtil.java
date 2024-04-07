package karpiuk.test.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

@Component
public class RedisUtil {

    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;
    private final Map<RedisDb, StringRedisTemplate> templateByDb;

    public RedisUtil(@Value("${spring.data.redis.host}") String redisHost,
                     @Value("${spring.data.redis.port}") int redisPort,
                     @Value("${spring.data.redis.password}") String redisPassword) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
        this.templateByDb = new EnumMap<>(RedisDb.class);
        stream(RedisDb.values()).forEach(this::createTemplate);
    }

    public enum RedisDb {
        DEFAULT,
        SECURITY
    }

    public StringRedisTemplate templateForDb(RedisDb db) {
        return templateByDb.get(db);
    }

    private void createTemplate(RedisDb db) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(db.ordinal());

        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        factory.afterPropertiesSet();

        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        template.setDefaultSerializer(new StringRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        templateByDb.put(db, template);
    }

    @PostConstruct
    private void testConnection() {
        templateByDb.forEach((db, template) -> {
            String result = requireNonNull(template.getConnectionFactory())
                    .getConnection().ping();
            if (!"PONG".equals(result)) {
                throw new BeanInitializationException(
                        "Failed to connect to Redis database. Please recheck Redis settings");
            }
        });
    }
}
