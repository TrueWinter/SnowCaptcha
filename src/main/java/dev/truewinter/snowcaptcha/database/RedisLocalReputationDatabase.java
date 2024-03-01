package dev.truewinter.snowcaptcha.database;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import inet.ipaddr.IPAddress;
import redis.clients.jedis.JedisPooled;

import java.security.NoSuchAlgorithmException;

public class RedisLocalReputationDatabase {
    private JedisPooled pool;

    public RedisLocalReputationDatabase(JedisPooled pool) {
        this.pool = pool;
    }

    private String formatKey(IPAddress prefix) {
        IPAddress prefix1 = prefix.toZeroHost().withoutPrefixLength();
        String prefixString;
        try {
            prefixString = Util.sha2Hash(prefix1.toString());
        } catch (NoSuchAlgorithmException e) {
            SnowCaptcha.getLogger().warn("SHA256 unsupported on this system, storing unhashed prefix");
            prefixString = prefix1.toString();
        }

        return String.format("rep:local:%s", prefixString);
    }

    public boolean isInDatabase(IPAddress prefix) {
        return pool.exists(formatKey(prefix));
    }

    public void addToDatabase(IPAddress prefix) {
        String key = formatKey(prefix);
        pool.set(key, "1");
        pool.expire(key, 10 * 60);
    }
}
