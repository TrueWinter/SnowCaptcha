package dev.truewinter.snowcaptcha.reputation;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import inet.ipaddr.IPAddress;

/**
 * Checks if the IP prefix (/24 for IPv4, /48 for IPv6) has completed a captcha recently.
 */
public class LocalReputation implements ReputationSource {
    public static IPAddress getPrefix(IPAddress ip) {
        return ip.setPrefixLength(ip.isIPv4() ? 24 : 48).toPrefixBlock();
    }

    @Override
    public boolean hasBadReputation(IPAddress ip) {
        return SnowCaptcha.getRedis().getRedisLocalReputationDatabase().isInDatabase(getPrefix(ip));
    }
}
