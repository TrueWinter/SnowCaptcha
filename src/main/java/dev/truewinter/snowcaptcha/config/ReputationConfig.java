package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;

public class ReputationConfig {
    private final ProjectHoneyPotConfig projectHoneyPotConfig;
    private final BGPToolsConfig bgpToolsConfig;

    public ReputationConfig(Section section) {
        this.projectHoneyPotConfig = new ProjectHoneyPotConfig(section.getSection("project_honey_pot"));
        this.bgpToolsConfig = new BGPToolsConfig(section.getSection("bgp_tools"));
    }

    public ProjectHoneyPotConfig getProjectHoneyPotConfig() {
        return projectHoneyPotConfig;
    }

    public BGPToolsConfig getBgpToolsConfig() {
        return bgpToolsConfig;
    }
}
