package dev.truewinter.snowcaptcha.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.challenge.ChallengeAnswer;
import dev.truewinter.snowcaptcha.challenge.SerializedChallenge;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

public class TokenRequest {
    private String token;
    private HashMap<String, ChallengeAnswer> challenges = new HashMap<>();
    @JsonIgnore
    private HashMap<String, SerializedChallenge> tokenData = new HashMap<>();
    private String sitekey;

    public TokenRequest(@JsonProperty("token") String token,
                        @JsonProperty("challenges") HashMap<String, String> hashMap,
                        @JsonProperty("sitekey") String sitekey) throws JsonProcessingException {
        this.token = token;
        this.sitekey = sitekey;

        if (token == null || token.isBlank()) {
            return;
        }

        if (sitekey == null || sitekey.isBlank()) {
            throw new TokenNotFoundException();
        }

        tokenData = SnowCaptcha.getRedis().getRedisTokenDatabase().getTokenData(sitekey, token)
                .orElse(null);

        if (tokenData == null) {
            throw new TokenNotFoundException();
        }

        hashMap.forEach((t, a) -> {
            SerializedChallenge s = tokenData.get(t);
            if (s != null) {
                challenges.put(t, new ChallengeAnswer(a, s.getChallenge()));
            }
        });
    }

    @Nullable
    public String getToken() {
        return token;
    }

    public boolean hasToken() {
        return token != null && !token.isBlank();
    }

    public HashMap<String, ChallengeAnswer> getChallenges() {
        return challenges;
    }

    public HashMap<String, SerializedChallenge> getTokenData() {
        return tokenData;
    }

    public String getSitekey() {
        return sitekey;
    }
}
