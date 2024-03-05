package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.util.List;

public class BGPToolsConfig {
    private final List<String> blacklist;
    private final List<String> whitelist;
    private final String api;

    public BGPToolsConfig(Section section) {
        this.blacklist = section.getStringList("blacklist");
        this.whitelist = section.getStringList("whitelist");
        this.api = section.getString("api", "https://bgp.tools");
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public String getApi() {
        return api;
    }
}
