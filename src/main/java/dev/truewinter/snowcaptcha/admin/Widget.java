package dev.truewinter.snowcaptcha.admin;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Widget {
    private final String sitekey;
    @JsonIgnore
    /*
     * In most cases, the secret key will be a hash. However, the unhashed version is returned to the
     * dashboard after a widget is created or edited to allow the secret key to be copied. The
     * hashedSecretKey boolean indicates whether the hashed or unhashed version is used.
     */
    private final String secretkey;
    private final String name;
    private final boolean hashedSecretKey;

    public Widget(String sitekey, String secretkey, String name, boolean hashedSecretKey) {
        this.sitekey = sitekey;
        this.secretkey = secretkey;
        this.name = name;
        this.hashedSecretKey = hashedSecretKey;
    }

    public String getSitekey() {
        return sitekey;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public boolean isHashedSecretKey() {
        return hashedSecretKey;
    }

    public String getName() {
        return name;
    }

    public static String createHash(String key) {
        return BCrypt.with(BCrypt.Version.VERSION_2A, LongPasswordStrategies.none())
                .hashToString(10, key.toCharArray());
    }

    public static boolean isCorrectKey(String key, Widget widget) {
        if (!widget.isHashedSecretKey()) {
            throw new IllegalStateException("A Widget object with a plain-text secret key was passed to isCorrectKey");
        }
        return BCrypt.verifyer(BCrypt.Version.VERSION_2A, LongPasswordStrategies.none())
                .verify(key.toCharArray(), widget.getSecretkey().toCharArray()).verified;
    }
}
