package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.util.Optional;

public class DevModeConfig {
    private final boolean enabled;
    private final boolean testPageEnabled;
    private String sitekey;
    private String secretkey;
    private String src;
    private String host;
    private Section overrides;

    public enum Override {
        IP("ip");

        private final String override;

        Override(String override) {
            this.override = override;
        }

        public String getOverride() {
            return override;
        }
    }

    public DevModeConfig(Section section) {
        this.enabled = section.getBoolean("enabled");
        this.testPageEnabled = section.getBoolean("test_page_enabled");
        this.sitekey = section.getString("sitekey");
        this.secretkey = section.getString("secretkey");
        this.src = section.getString("src");
        this.host = section.getString("host");
        this.overrides = section.getSection("overrides");
    }

    public DevModeConfig() {
        this.enabled = false;
        this.testPageEnabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isTestPageEnabled() {
        return testPageEnabled;
    }

    public String getSitekey() {
        return sitekey;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public String getSrc() {
        return src;
    }

    public String getHost() {
        return host;
    }

    public Optional<String> getOverride(Override override) {
        if (!enabled) {
            return Optional.empty();
        }

        return overrides.getOptionalString(override.getOverride());
    }
}
