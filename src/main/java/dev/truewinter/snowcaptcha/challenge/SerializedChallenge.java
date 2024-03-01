package dev.truewinter.snowcaptcha.challenge;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SerializedChallenge {
    private String type;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "type",
            visible = true
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ProofOfWorkChallenge.class, name = Challenge.PROOF_OF_WORK),
            //@JsonSubTypes.Type(value = RegionVisualChallenge.class, name = "REGION"),
            @JsonSubTypes.Type(value = TextVisualChallenge.class, name = "TEXT")
    })
    private Challenge challenge;

    public SerializedChallenge() {}

    public SerializedChallenge(@NotNull String type, @NotNull Challenge challenge) {
        this.type = type;
        this.challenge = challenge;
    }

    public final String getType() {
        return type;
    }

    public final Challenge getChallenge() {
        return challenge;
    }
}
