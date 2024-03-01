package dev.truewinter.snowcaptcha.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenValidationRequest {
    private String sitekey;
    private String secretkey;
    private String token;

    public TokenValidationRequest() {}

    public String getSitekey() {
        return sitekey;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public String getToken() {
        return token;
    }
}
