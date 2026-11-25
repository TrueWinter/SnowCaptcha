package dev.truewinter.snowcaptcha.storage;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.RegionChallenge;
import dev.truewinter.snowcaptcha.database.MysqlRegionChallengeDatabase;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class S3AvailabilityMonitor {
    private final S3Storage s3;
    private final MysqlRegionChallengeDatabase database;
    private final int intervalSeconds;
    private ScheduledExecutorService executor;

    public S3AvailabilityMonitor(S3Storage s3, MysqlRegionChallengeDatabase database, int intervalSeconds) {
        this.s3 = s3;
        this.database = database;
        this.intervalSeconds = intervalSeconds;
    }

    public void start() {
        if (intervalSeconds <= 0) {
            s3.setAvailable(true);
            return;
        }

        checkAvailability();

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "s3-availability");
            thread.setDaemon(true);
            return thread;
        });

        executor.scheduleWithFixedDelay(this::checkAvailability, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    private void checkAvailability() {
        boolean wasAvailable = s3.isAvailable();
        boolean isAvailable;

        try {
            Optional<RegionChallenge> challenge = database.getRandomRegionChallenge();
            if (challenge.isPresent()) {
                s3.headObject(challenge.get().getObjectKey());
            }

            isAvailable = true;
        } catch (Exception e) {
            isAvailable = false;
            if (wasAvailable) {
                SnowCaptcha.getLogger().warn("S3 is unavailable, falling back to text challenges", e);
            }
        }

        s3.setAvailable(isAvailable);

        if (isAvailable && !wasAvailable) {
            SnowCaptcha.getLogger().info("S3 is available again");
        }
    }

    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
