package dev.truewinter.snowcaptcha;

import dev.truewinter.snowcaptcha.config.DevModeConfig;
import dev.truewinter.snowcaptcha.config.HttpIpConfig;
import io.javalin.http.Context;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class Util {
    // https://stackoverflow.com/a/15954821
    public static String getInstallPath() {
        Path relative = Paths.get("");
        return relative.toAbsolutePath().toString();
    }

    // https://stackoverflow.com/a/50381020
    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

    public static String generateRandomHexString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length / 2];
        random.nextBytes(bytes);
        return Hex.encodeHexString(bytes).toUpperCase();
    }

    public static byte[] sha1Hash(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha2Hash(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Hex.encodeHexString(md.digest(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static String getIp(HttpIpConfig httpIpConfig, DevModeConfig devModeConfig, Context ctx) {
        Optional<String> overrideIp = devModeConfig.getOverride(DevModeConfig.Override.IP);
        if (overrideIp.isPresent() && !overrideIp.get().isBlank()) {
            return overrideIp.get();
        }

        if (httpIpConfig.shouldReadFromHeader()) {
            String headerIp = ctx.header(httpIpConfig.getHeaderName());
            if (headerIp == null || headerIp.isBlank()) {
                return ctx.ip();
            }

            return headerIp;
        }

        return ctx.ip();
    }

    @Contract("null -> true; !null -> false")
    public static boolean isBlank(@Nullable String string) {
        if (string == null) return true;
        return string.isBlank();
    }
}
