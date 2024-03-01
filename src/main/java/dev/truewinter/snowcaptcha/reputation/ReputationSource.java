package dev.truewinter.snowcaptcha.reputation;

import inet.ipaddr.IPAddress;

public interface ReputationSource {
    boolean hasBadReputation(IPAddress ip);
}
