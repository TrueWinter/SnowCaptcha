package dev.truewinter.snowcaptcha.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;

public class HttpIpConfig {
    private final boolean readFromHeader;
    private final String headerName;

    public HttpIpConfig(Section section) {
        this.readFromHeader = section.getBoolean("read_from_header");
        this.headerName = section.getString("header_name");
    }

    public boolean shouldReadFromHeader() {
        return readFromHeader;
    }

    public String getHeaderName() {
        return headerName;
    }
}
