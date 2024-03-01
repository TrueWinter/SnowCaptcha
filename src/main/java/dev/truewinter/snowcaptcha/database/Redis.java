package dev.truewinter.snowcaptcha.database;

import dev.truewinter.snowcaptcha.config.Config;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.config.RedisConfig;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

public class Redis {
    private final RedisTokenDatabase redisTokenDatabase;
    private final RedisLocalReputationDatabase redisLocalReputationDatabase;
    private final RedisBGPToolsReputationDatabase redisBGPToolsReputationDatabase;
    private final RedisPHPReputationDatabase redisPHPReputationDatabase;

    public Redis(RedisConfig config) {
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWait(Duration.ofSeconds(1));
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));

        DefaultJedisClientConfig defaultJedisClientConfig = DefaultJedisClientConfig.builder()
                .socketTimeoutMillis(5000)
                .connectionTimeoutMillis(5000)
                .user(config.getUser())
                .password(config.getPassword())
                .build();

        HostAndPort hostAndPort = new HostAndPort(config.getHost(), config.getPort());

        JedisPooled pool = new JedisPooled(hostAndPort, defaultJedisClientConfig, poolConfig);
        SnowCaptcha.getLogger().info("Connected to Redis");

        redisTokenDatabase = new RedisTokenDatabase(pool);
        redisLocalReputationDatabase = new RedisLocalReputationDatabase(pool);
        redisBGPToolsReputationDatabase = new RedisBGPToolsReputationDatabase(pool);
        redisPHPReputationDatabase = new RedisPHPReputationDatabase(pool);
    }

    public RedisTokenDatabase getRedisTokenDatabase() {
        return redisTokenDatabase;
    }

    public RedisLocalReputationDatabase getRedisLocalReputationDatabase() {
        return redisLocalReputationDatabase;
    }

    public RedisBGPToolsReputationDatabase getRedisBGPToolsReputationDatabase() {
        return redisBGPToolsReputationDatabase;
    }

    public RedisPHPReputationDatabase getRedisPHPReputationDatabase() {
        return redisPHPReputationDatabase;
    }
}
