package dev.truewinter.snowcaptcha.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.challenge.Challenge;
import dev.truewinter.snowcaptcha.challenge.SerializedChallenge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TokenResponse {
    private final boolean success;
    private final HashMap<String, SerializedChallenge> challenges;
    private final String token;
    @JsonIgnore
    private final String sitekey;

    public TokenResponse(boolean success, @NotNull HashMap<String, SerializedChallenge> challenges,
                         @Nullable String sitekey, @Nullable String token) {
        this.success = challenges.isEmpty() || success;
        this.challenges = challenges;
        this.token = token;
        this.sitekey = sitekey;
    }

    public boolean isSuccess() {
        return success;
    }

    public HashMap<String, SerializedChallenge> getChallenges() {
        return challenges;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    @JsonProperty("expiry")
    public int getExpiry() {
        return (int) (Math.floor((double) System.currentTimeMillis() / 1000L) +
                SnowCaptcha.getRedis().getRedisTokenDatabase().getTokenExpiry(sitekey, token));
    }
}
