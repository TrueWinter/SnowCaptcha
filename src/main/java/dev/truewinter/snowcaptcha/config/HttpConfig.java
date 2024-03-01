package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.truewinter.snowcaptcha.SnowCaptcha;

public class HttpConfig {
    private final int port;
    private final String secret;
    private final String privacyRedirect;
    private final HttpIpConfig httpIpConfig;

    public HttpConfig(Section http) {
        this.port = http.getInt("port");
        this.secret = http.getString("secret");
        this.privacyRedirect = http.getString("privacy_redirect");
        this.httpIpConfig = new HttpIpConfig(http.getSection("ip"));

        if (secret.equals("alongrandomvalue")) {
            SnowCaptcha.getLogger().warn("Using default secret, change this in the config file");
        }
    }

    public int getPort() {
        return port;
    }

    public String getSecret() {
        return secret;
    }

    public String getPrivacyRedirect() {
        return privacyRedirect;
    }

    public HttpIpConfig getHttpIpConfig() {
        return httpIpConfig;
    }
}
