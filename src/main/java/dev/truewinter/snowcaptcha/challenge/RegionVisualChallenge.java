package dev.truewinter.snowcaptcha.challenge;

// Region challenges will be introduced in a future version
public class RegionVisualChallenge extends VisualChallenge {
    @Override
    public boolean isValid(ChallengeAnswer answer) {
        return false;
    }

    @Override
    public Challenge recreate() throws Exception {
        return null;
    }
}
