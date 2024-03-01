package dev.truewinter.snowcaptcha.database;

import redis.clients.jedis.JedisPooled;

public class RedisPHPReputationDatabase {
    private final String key = "rep:php:block";
    private JedisPooled pool;

    public RedisPHPReputationDatabase(JedisPooled pool) {
        this.pool = pool;
    }

    public boolean isBlocked() {
        return pool.exists(key);
    }

    public void block() {
        pool.set(key, "1");
        pool.expire(key, 30 * 60);
    }
}
