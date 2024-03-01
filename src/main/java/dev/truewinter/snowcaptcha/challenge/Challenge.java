package dev.truewinter.snowcaptcha.challenge;

public interface Challenge {
    String PROOF_OF_WORK = "PROOF_OF_WORK";
    String REGION = "REGION";
    String TEXT = "TEXT";

    boolean isValid(ChallengeAnswer answer);
    Challenge recreate() throws Exception;
}
