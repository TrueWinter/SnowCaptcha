package dev.truewinter.snowcaptcha.admin;

public class RegionChallenge {
    private final long id;
    private final String objectKey;
    private final String contentType;
    private final String instructions;
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public RegionChallenge(long id, String objectKey, String contentType, String instructions,
                           double x, double y, double width, double height) {
        this.id = id;
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.instructions = instructions;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public long getId() {
        return id;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getContentType() {
        return contentType;
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
}
