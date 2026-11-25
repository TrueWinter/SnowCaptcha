package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.RegionChallenge;
import dev.truewinter.snowcaptcha.storage.S3Storage;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

final class RegionChallengeUtil {
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 10_000;
    private static final long MAX_IMAGE_PIXELS = 20_000_000;

    record FormData(String instructions, double x, double y, double width, double height) {}

    record UploadedImage(String objectKey, String contentType) {}

    private RegionChallengeUtil() {}

    static FormData parseForm(Context ctx) {
        String instructions = ctx.formParam("instructions");
        if (Util.isBlank(instructions)) {
            throw new IllegalArgumentException("Instructions are required");
        }

        instructions = instructions.trim();
        if (instructions.length() > 255) {
            throw new IllegalArgumentException("Instructions must be 255 characters or fewer");
        }

        double x = parseCoordinate(ctx.formParam("x"));
        double y = parseCoordinate(ctx.formParam("y"));
        double width = parseCoordinate(ctx.formParam("width"));
        double height = parseCoordinate(ctx.formParam("height"));

        if (width < 0.005 || height < 0.005) {
            throw new IllegalArgumentException("Please select a larger answer area");
        }

        if (x < 0 || y < 0 || x + width > 1.0001 || y + height > 1.0001) {
            throw new IllegalArgumentException("The answer area must be within the image");
        }

        return new FormData(instructions, x, y, width, height);
    }

    private static double parseCoordinate(String value) {
        if (Util.isBlank(value)) {
            throw new IllegalArgumentException("The answer area is missing");
        }

        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed)) {
                throw new NumberFormatException();
            }

            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The answer area is invalid");
        }
    }

    static UploadedImage uploadImage(UploadedFile file) throws Exception {
        if (file == null || file.size() == 0) {
            throw new IllegalArgumentException("An image is required");
        }

        if (file.size() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Image must be 5MB or smaller");
        }

        byte[] bytes;
        try (InputStream inputStream = file.content()) {
            bytes = inputStream.readAllBytes();
        }

        if (bytes.length == 0) {
            throw new IllegalArgumentException("An image is required");
        }

        if (bytes.length > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Image must be 5MB or smaller");
        }

        String contentType = validateImage(bytes);

        S3Storage s3 = SnowCaptcha.getS3();
        if (s3 == null) {
            throw new IllegalStateException("S3 is not configured");
        }

        String extension = contentType.equals("image/png") ? "png" : "jpg";
        String prefix = s3.getKeyPrefix();
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix += "/";
        }

        String objectKey = prefix + UUID.randomUUID() + "." + extension;
        s3.putObject(objectKey, bytes, contentType);

        return new UploadedImage(objectKey, contentType);
    }

    private static String validateImage(byte[] bytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) {
                throw new IllegalArgumentException("Image could not be read");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Image could not be read");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);

                String format = reader.getFormatName().toLowerCase();
                String contentType;
                if (format.equals("jpeg") || format.equals("jpg")) {
                    contentType = "image/jpeg";
                } else if (format.equals("png")) {
                    contentType = "image/png";
                } else {
                    throw new IllegalArgumentException("Image must be a JPEG or PNG");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION ||
                        (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IllegalArgumentException("Image dimensions are too large");
                }

                if (reader.read(0) == null) {
                    throw new IllegalArgumentException("Image could not be read");
                }

                return contentType;
            } finally {
                reader.dispose();
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Image could not be read");
        }
    }

    static Map<String, Object> toViewModel(RegionChallenge challenge) {
        Map<String, Object> model = new HashMap<>();
        model.put("id", challenge.getId());
        model.put("instructions", challenge.getInstructions());
        model.put("x", challenge.getX());
        model.put("y", challenge.getY());
        model.put("width", challenge.getWidth());
        model.put("height", challenge.getHeight());
        model.put("imageUrl", getImageUrl(challenge));
        return model;
    }

    private static String getImageUrl(RegionChallenge challenge) {
        S3Storage s3 = SnowCaptcha.getS3();
        if (s3 == null) {
            return "";
        }

        try {
            return s3.presignedGetUrl(challenge.getObjectKey(), challenge.getContentType(),
                    Duration.ofHours(24));
        } catch (Exception e) {
            SnowCaptcha.getLogger().warn("Failed to generate region challenge image URL", e);
            return "";
        }
    }

    static void cleanupUploadedImage(String objectKey, boolean writeAttempted) {
        if (objectKey == null) return;

        if (!writeAttempted) {
            deleteImageQuietly(objectKey);
            return;
        }

        try {
            if (!SnowCaptcha.getMysql().getMysqlRegionChallengeDatabase().isObjectKeyReferenced(objectKey)) {
                deleteImageQuietly(objectKey);
            }
        } catch (Exception e) {
            SnowCaptcha.getLogger().warn(
                    "Failed to check whether an uploaded region challenge image is referenced, leaving it in S3", e);
        }
    }

    static void deleteImageQuietly(String objectKey) {
        S3Storage s3 = SnowCaptcha.getS3();
        if (s3 == null) return;

        try {
            s3.deleteObject(objectKey);
        } catch (Exception e) {
            SnowCaptcha.getLogger().warn("Failed to delete region challenge image from S3", e);
        }
    }
}
