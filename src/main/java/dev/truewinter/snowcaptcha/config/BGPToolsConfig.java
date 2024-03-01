package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.util.List;

public class BGPToolsConfig {
    private final List<String> blacklist;
    private final List<String> whitelist;

    public BGPToolsConfig(Section section) {
        this.blacklist = section.getStringList("blacklist");
        this.whitelist = section.getStringList("whitelist");
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }
}
