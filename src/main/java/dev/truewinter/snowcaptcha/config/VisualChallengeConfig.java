package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;

public class VisualChallengeConfig {
    private final boolean regionEnabled;

    public VisualChallengeConfig(Section section) {
        Section region = section.getSection("region");
        this.regionEnabled = region != null && region.getBoolean("enabled", false);
    }

    public VisualChallengeConfig() {
        this.regionEnabled = false;
    }

    public boolean isRegionEnabled() {
        return regionEnabled;
    }
}
