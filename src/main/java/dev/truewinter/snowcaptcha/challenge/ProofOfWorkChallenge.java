package dev.truewinter.snowcaptcha.challenge;

import dev.truewinter.snowcaptcha.Util;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class ProofOfWorkChallenge implements Challenge {
    private String challenge;
    private int difficulty;

    public ProofOfWorkChallenge() {}

    public ProofOfWorkChallenge(@NotNull String challenge, @NotNull int difficulty) {
        this.challenge = challenge;
        this.difficulty = difficulty;
    }

    public static ProofOfWorkChallenge create() {
        return new ProofOfWorkChallenge(Util.generateRandomString(16),13);
    }

    public ProofOfWorkChallenge recreate() throws IOException {
        return create();
    }

    public String getChallenge() {
        return challenge;
    }

    public int getDifficulty() {
        return difficulty;
    }

    @Override
    public boolean isValid(ChallengeAnswer answer) {
        if (!answer.getAnswer().contains(":")) return false;
        Optional<String> clientChallenge = Arrays.stream(answer.getAnswer().split(":", 2)).findFirst();

        if (clientChallenge.isEmpty() || !clientChallenge.get().equals(challenge)) {
            return false;
        }

        try {
            byte[] hash = Util.sha1Hash(answer.getAnswer());

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            }

            if (sb.substring(0, difficulty).equals("0".repeat(difficulty))) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
