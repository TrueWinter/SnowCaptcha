package dev.truewinter.snowcaptcha.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.Optional;

public class RedisBGPToolsReputationDatabase {
    private enum KEY_TYPE {
        EXPIRY,
        DATA
    }
    private JedisPooled pool;

    public RedisBGPToolsReputationDatabase(JedisPooled pool) {
        this.pool = pool;
    }

    private String formatKey(KEY_TYPE keyType) {
        return String.format("rep:bgptools:%s", keyType.toString());
    }

    public Optional<List<String>> getData() throws JsonProcessingException {
        String data = pool.get(formatKey(KEY_TYPE.DATA));
        if (data == null) return Optional.empty();

        return Optional.of(new ObjectMapper().readValue(data, TypeFactory.defaultInstance()
                .constructCollectionLikeType(List.class, String.class)));
    }

    public void setData(List<String> data) throws JsonProcessingException {
        pool.set(formatKey(KEY_TYPE.DATA), new ObjectMapper().writeValueAsString(data));
    }

    public boolean isDataExpired() {
        return !pool.exists(formatKey(KEY_TYPE.EXPIRY));
    }

    public void updateExpiry() {
        String key = formatKey(KEY_TYPE.EXPIRY);
        pool.set(key, String.valueOf(System.currentTimeMillis()));
        pool.expire(key, 2 * 60 * 60);
    }
}
