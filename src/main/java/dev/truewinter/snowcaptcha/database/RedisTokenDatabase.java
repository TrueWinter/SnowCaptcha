package dev.truewinter.snowcaptcha.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.challenge.SerializedChallenge;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.Optional;

public class RedisTokenDatabase {
    private JedisPooled pool;

    public RedisTokenDatabase(JedisPooled pool) {
        this.pool = pool;
    }

    private String formatToken(String sitekey, String token) {
        return String.format("token:%s:%s", sitekey, token);
    }

    public Optional<HashMap<String, SerializedChallenge>> getTokenData(String sitekey, String token) throws JsonProcessingException {
        String jsonString = pool.get(formatToken(sitekey, token));

        if (jsonString == null || jsonString.isBlank()) {
            return Optional.empty();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return Optional.of(objectMapper.readValue(jsonString,
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, SerializedChallenge.class)));
    }

    public void setTokenData(String sitekey, String token, HashMap<String, SerializedChallenge> challenges)
            throws JsonProcessingException {
        final String formattedToken = formatToken(sitekey, token);
        // The TTL should only be updated when the token is first created and after the captcha is completed
        boolean shouldUpdateExpiry = !pool.exists(formattedToken);
        long updateTime = !shouldUpdateExpiry ? getTokenExpiry(sitekey, token) : 0;

        pool.set(formattedToken, JsonMapper.builder()
                .configure(MapperFeature.USE_ANNOTATIONS, false)
                .build()
                .writeValueAsString(challenges));

        if (shouldUpdateExpiry) {
            updateTokenExpiry(sitekey, token);
        } else {
            updateTokenExpiry(sitekey, token, updateTime);
        }
    }

    public String generateRandomToken(String sitekey) {
        String token = Util.generateRandomString(64);
        if (pool.exists(formatToken(sitekey, token))) {
            return generateRandomToken(sitekey);
        }

        return token;
    }

    public void updateTokenExpiry(String sitekey, String token) {
        updateTokenExpiry(sitekey, token, 300);
    }

    public void updateTokenExpiry(String sitekey, String token, long seconds) {
        pool.expire(formatToken(sitekey, token), seconds);
    }

    public long getTokenExpiry(String sitekey, String token) {
        return pool.ttl(formatToken(sitekey, token));
    }

    public void removeKey(String sitekey, String token) {
        pool.del(formatToken(sitekey, token));
    }
}
