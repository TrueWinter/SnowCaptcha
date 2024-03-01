package dev.truewinter.snowcaptcha.reputation;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import inet.ipaddr.IPAddress;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class ReputationTester {
    private static ReputationTester reputationTester;
    private static final LinkedList<ReputationSource> reputationSources = new LinkedList<>();

    private ReputationTester() {
        // LinkedList and in this order to ensure that Project Honey Pot is only checked if actually needed.
        // This cuts back on requests to Project Honey Pot and keeps their server resources available for other
        // projects that need it.
        reputationSources.add(new LocalReputation());
        reputationSources.add(new BGPToolsReputation());
        reputationSources.add(new ProjectHoneyPotReputation());
    }

    public static ReputationTester getInstance() {
        if (reputationTester == null) {
            reputationTester = new ReputationTester();
        }
        return reputationTester;
    }

    public boolean hasBadReputation(IPAddress ip) {
        for (ReputationSource reputationSource : reputationSources) {
            boolean rep = reputationSource.hasBadReputation(ip);
            if (rep) {
                try {
                    SnowCaptcha.getDebugLogger().getLogger()
                            .info(Util.sha2Hash(ip.toString()) + " has bad reputation, reason: " +
                                    reputationSource.getClass().getSimpleName());
                } catch (NoSuchAlgorithmException ignored) {}
                return true;
            }
        }

        return false;
    }

    public void updateReputation(IPAddress ip) {
        SnowCaptcha.getRedis().getRedisLocalReputationDatabase().addToDatabase(LocalReputation.getPrefix(ip));
    }
}
