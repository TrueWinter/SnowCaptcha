package dev.truewinter.snowcaptcha.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.truewinter.snowcaptcha.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

public class TextVisualChallenge extends VisualChallenge {
    private record BgMap(String bg, Color color) {}
    private static final BgMap[] backgrounds = new BgMap[]{
            new BgMap("bg1.jpg", Color.GREEN),
            new BgMap("bg2.jpg", Color.MAGENTA),
            new BgMap("bg3.jpg", Color.PINK)
    };
    private static final int PADDING = 32;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String text;
    private String img;

    public TextVisualChallenge() {}

    public TextVisualChallenge(String text) throws IOException {
        this.text = text;

        BgMap bgMap = backgrounds[(int) Math.floor(Math.random() * backgrounds.length)];
        BufferedImage image = ImageIO.read(Objects.requireNonNull(getClass()
                .getResource("/web/public/captcha/backgrounds/" + bgMap.bg())));
        Graphics g = image.getGraphics();
        Font font = getMaxFontSize(g, text, 24);
        FontMetrics metrics = g.getFontMetrics(font);
        int posX = (270 - metrics.stringWidth(text)) / 2;
        int posY = (100 - metrics.getHeight()) / 2 + metrics.getAscent();
        g.setFont(font);
        g.setColor(bgMap.color());
        g.drawString(text, posX, posY);
        g.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", os);
        this.img = Base64.getEncoder().encodeToString(os.toByteArray());
    }

    public static TextVisualChallenge create() throws IOException {
        return new TextVisualChallenge(Util.generateRandomHexString(6));
    }

    public TextVisualChallenge recreate() throws IOException {
        return create();
    }

    private Font getMaxFontSize(Graphics g, String text, int size) {
        Font font = new Font("SansSerif", Font.BOLD, size);
        FontMetrics metrics = g.getFontMetrics(font);
        if (metrics.stringWidth(text) + (PADDING * 2) < 270) {
            return getMaxFontSize(g, text, size + 2);
        }

        return font;
    }

    public String getText() {
        return text;
    }

    public String getImg() {
        return img;
    }

    @Override
    public boolean isValid(ChallengeAnswer answer) {
        if (answer.getAnswer().equals("GETNEW")) return false;
        return answer.getAnswer()
                .toUpperCase()
                // Replace non-hexadecimal characters with the closest-looking hexadecimal character
                .replace("I", "1")
                .replace("L", "1")
                .replace("O", "0")
                .replace("Q", "0")
                .equalsIgnoreCase(text);
    }
}
