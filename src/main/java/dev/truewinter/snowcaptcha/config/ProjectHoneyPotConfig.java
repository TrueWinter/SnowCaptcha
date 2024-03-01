package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.truewinter.snowcaptcha.Util;

import java.util.List;

public class ProjectHoneyPotConfig {
    private final String apiKey;
    private final List<String> resolvers;

    public ProjectHoneyPotConfig(Section section) {
        this.apiKey = section.getString("api_key");
        this.resolvers = section.getStringList("resolvers");
    }

    public boolean isEnabled() {
        return !Util.isBlank(apiKey) && !apiKey.equals("abcdefghijkl");
    }

    public String getApiKey() {
        return apiKey;
    }

    public List<String> getResolvers() {
        return resolvers;
    }
}
