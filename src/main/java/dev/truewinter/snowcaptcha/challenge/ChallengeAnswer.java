package dev.truewinter.snowcaptcha.challenge;

public class ChallengeAnswer {
    private final String answer;
    private final Challenge challenge;

    public ChallengeAnswer(String answer, Challenge challenge) {
        this.answer = answer;
        this.challenge = challenge;
    }

    public final String getAnswer() {
        return answer;
    }

    public Challenge getChallenge() {
        return challenge;
    }
}
