package dev.truewinter.snowcaptcha.token;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException() {
        super("Token not found");
    }
}
