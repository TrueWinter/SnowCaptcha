package dev.truewinter.snowcaptcha.reputation;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.config.ProjectHoneyPotConfig;
import dev.truewinter.snowcaptcha.database.RedisPHPReputationDatabase;
import inet.ipaddr.IPAddress;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;

/**
 * Checks if the IPv4 address is on the Project Honey Pot blacklist.
 * This will currently return false for IPv6 addresses as Project Honey Pot does not yet support IPv6.
 * This will be changed in the future, either once Project Honey Pot supports IPv6 or when I
 * find a similar project that does.
 */
public class ProjectHoneyPotReputation implements ReputationSource {
    @Override
    public boolean hasBadReputation(IPAddress ip) {
        ProjectHoneyPotConfig projectHoneyPotConfig = SnowCaptcha.getReputationConfig().getProjectHoneyPotConfig();
        if (ip.isIPv6()) return false;
        if (!projectHoneyPotConfig.isEnabled()) return false;

        RedisPHPReputationDatabase redisPHPReputationDatabase = SnowCaptcha.getRedis().getRedisPHPReputationDatabase();
        if (redisPHPReputationDatabase.isBlocked()) return false;

        ExtendedResolver extendedResolver;
        try {
            extendedResolver = new ExtendedResolver(new String[]{});
            for (String resolver : projectHoneyPotConfig.getResolvers()) {
                    extendedResolver.addResolver(new SimpleResolver(resolver));
            }
        } catch (UnknownHostException e) {
            SnowCaptcha.getLogger().warn("Invalid resolver", e);
            return false;
        }

        if (extendedResolver.getResolvers().length == 0) return false;

        extendedResolver.setLoadBalance(true);
        extendedResolver.setTimeout(Duration.ofSeconds(3));
        extendedResolver.setRetries(1);

        Lookup lookup;
        try {
            lookup = new Lookup(String.format("%s.%s.dnsbl.httpbl.org", projectHoneyPotConfig.getApiKey(),
                    ip.reverseSegments().toString()), Type.A);
        } catch (TextParseException e) {
            return false;
        }

        lookup.setCache(null);
        lookup.setResolver(extendedResolver);
        lookup.run();

        if (lookup.getResult() == Lookup.SUCCESSFUL) {
            return true;
        } else if (lookup.getResult() == Lookup.TRY_AGAIN || lookup.getResult() == Lookup.UNRECOVERABLE) {
            redisPHPReputationDatabase.block();
        }

        return false;
    }
}
