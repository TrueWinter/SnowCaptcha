package dev.truewinter.snowcaptcha.admin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;

import java.time.Instant;

public class LoginCookie {
    public static final int EXPIRES_IN = 24 * 60 * 60;
    private final String username;
    private final String csrf;

    public LoginCookie(String username, String csrf) {
        this.username = username;
        this.csrf = csrf;
    }

    public LoginCookie(String username) {
        this.username = username;
        this.csrf = Util.generateRandomString(32);
    }

    public String getUsername() {
        return username;
    }

    public String getCsrf() {
        return csrf;
    }

    public String getJWT() {
        Algorithm algorithm = Algorithm.HMAC256(SnowCaptcha.getSecret());
        return JWT.create()
                .withExpiresAt(Instant.ofEpochSecond((long) (System.currentTimeMillis() / 1000.0) + EXPIRES_IN))
                .withClaim("username", username)
                .withClaim("csrf", csrf)
                .sign(algorithm);
    }

    public static LoginCookie jwtToLoginCookie(String jwtString) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(SnowCaptcha.getSecret());
        JWTVerifier verifier = JWT.require(algorithm).acceptLeeway(10).build();
        DecodedJWT jwt = verifier.verify(jwtString);
        String username = jwt.getClaim("username").asString();
        String csrf = jwt.getClaim("csrf").asString();

        return new LoginCookie(username, csrf);
    }
}
