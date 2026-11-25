package dev.truewinter.snowcaptcha.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.RegionChallenge;
import dev.truewinter.snowcaptcha.storage.S3Storage;

import java.time.Duration;

public class RegionVisualChallenge extends VisualChallenge {
    private static final double TOLERANCE = 0.01;

    private String img;
    private String instructions;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private double x;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private double y;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private double width;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private double height;

    public RegionVisualChallenge() {}

    public RegionVisualChallenge(String img, String instructions, double x, double y,
                                 double width, double height) {
        this.img = img;
        this.instructions = instructions;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static RegionVisualChallenge create() throws Exception {
        S3Storage s3 = SnowCaptcha.getS3();
        if (s3 == null) {
            throw new IllegalStateException("S3 is not configured");
        }

        if (!s3.isAvailable()) {
            throw new IllegalStateException("S3 is unavailable");
        }

        RegionChallenge regionChallenge = SnowCaptcha.getMysql().getMysqlRegionChallengeDatabase()
                .getRandomRegionChallenge()
                .orElseThrow(() -> new IllegalStateException("No region challenges available"));

        String img = s3.presignedGetUrl(regionChallenge.getObjectKey(),
                regionChallenge.getContentType(), Duration.ofMinutes(10));

        return new RegionVisualChallenge(
                img,
                regionChallenge.getInstructions(),
                regionChallenge.getX(),
                regionChallenge.getY(),
                regionChallenge.getWidth(),
                regionChallenge.getHeight()
        );
    }

    @Override
    public RegionVisualChallenge recreate() throws Exception {
        return create();
    }

    public String getImg() {
        return img;
    }

    public String getInstructions() {
        return instructions;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public boolean isValid(ChallengeAnswer answer) {
        String value = answer.getAnswer();
        if (value == null || value.equals("GETNEW")) return false;

        String[] parts = value.split(",", -1);
        if (parts.length != 2) return false;

        double clickX;
        double clickY;
        try {
            clickX = Double.parseDouble(parts[0]);
            clickY = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (!Double.isFinite(clickX) || !Double.isFinite(clickY)) return false;

        return clickX >= (x - TOLERANCE) && clickX <= (x + width + TOLERANCE) &&
                clickY >= (y - TOLERANCE) && clickY <= (y + height + TOLERANCE);

    }
}
