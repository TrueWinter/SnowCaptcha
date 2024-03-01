package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.Widget;
import dev.truewinter.snowcaptcha.challenge.Challenge;
import dev.truewinter.snowcaptcha.challenge.ProofOfWorkChallenge;
import dev.truewinter.snowcaptcha.challenge.SerializedChallenge;
import dev.truewinter.snowcaptcha.challenge.TextVisualChallenge;
import dev.truewinter.snowcaptcha.config.DevModeConfig;
import dev.truewinter.snowcaptcha.config.HttpIpConfig;
import dev.truewinter.snowcaptcha.reputation.ReputationTester;
import dev.truewinter.snowcaptcha.token.TokenNotFoundException;
import dev.truewinter.snowcaptcha.token.TokenRequest;
import dev.truewinter.snowcaptcha.token.TokenResponse;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;

import java.util.HashMap;
import java.util.Optional;

public class GetTokenRoute {
    private static HttpIpConfig httpIpConfig;
    private static DevModeConfig devModeConfig;

    public static void init(HttpIpConfig httpIpConfig, DevModeConfig devModeConfig) {
        GetTokenRoute.httpIpConfig = httpIpConfig;
        GetTokenRoute.devModeConfig = devModeConfig;
    }

    public static void before(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");

        if (httpIpConfig == null || devModeConfig == null) {
            SnowCaptcha.getDebugLogger().getLogger().error("Received request before configs were initialized");
            throw new InternalServerErrorResponse();
        }
    }

    public static void post(Context ctx) throws Exception {
        TokenRequest tokenRequest;
        try {
            tokenRequest = ctx.bodyAsClass(TokenRequest.class);
        } catch (Exception e) {
            if (e.getCause() instanceof TokenNotFoundException) {
                throw new NotFoundResponse("Token not found");
            }
            throw new BadRequestResponse();
        }

        Optional<Widget> widget = SnowCaptcha.getMysql().getMysqlWidgetDatabase().getWidget(tokenRequest.getSitekey());
        if (widget.isEmpty()) {
            SnowCaptcha.getDebugLogger().getLogger().warn("Received request with invalid sitekey");
            throw new BadRequestResponse("Invalid sitekey");
        }

        HashMap<String, SerializedChallenge> challenges = new HashMap<>();

        String token;
        IPAddress ip = new IPAddressString(Util.getIp(httpIpConfig, devModeConfig, ctx)).toAddress();

        boolean tokenHadUnsolvedChallenges = false;
        if (tokenRequest.hasToken()) {
            HashMap<String, SerializedChallenge> tokenData = tokenRequest.getTokenData();
            if (!tokenData.isEmpty()) {
                tokenHadUnsolvedChallenges = true;
            }

            tokenRequest.getChallenges().forEach((key, answer) -> {
                SerializedChallenge challenge = tokenData.get(key);
                if (challenge != null) {
                    if (!answer.getChallenge().isValid(answer)) {
                        try {
                            challenges.put(Util.generateRandomString(32), new SerializedChallenge(
                                    challenge.getType(), answer.getChallenge().recreate()
                            ));

                            tokenData.remove(key);
                        } catch (Exception e) {
                            SnowCaptcha.getLogger().error("Failed to recreate challange", e);
                        }
                    } else {
                        tokenData.remove(key);
                    }
                }
            });

            challenges.putAll(tokenData);
            token = tokenRequest.getToken();
        } else {
            SnowCaptcha.getDebugLogger().getLogger().info("Received new request from " + Util.sha2Hash(ip.toString()));

            if (ReputationTester.getInstance().hasBadReputation(ip)) {
                challenges.put(Util.generateRandomString(32),
                        new SerializedChallenge(Challenge.TEXT, TextVisualChallenge.create()));
            }

            challenges.put(Util.generateRandomString(32), new SerializedChallenge(
                    Challenge.PROOF_OF_WORK, ProofOfWorkChallenge.create()));

            /*
                Tokens are intentionally not linked to the user's IP address as dual-stack users may
                switch between IPv4 and IPv6 between requests and mobile users often have their
                IP addresses changed when switching between phone towers.
             */
            token = SnowCaptcha.getRedis().getRedisTokenDatabase().generateRandomToken(tokenRequest.getSitekey());
        }

        TokenResponse tokenResponse = new TokenResponse(false, challenges,
                tokenRequest.getSitekey(), token);

        SnowCaptcha.getRedis().getRedisTokenDatabase().setTokenData(tokenRequest.getSitekey(), token, tokenResponse.getChallenges());
        if (tokenResponse.isSuccess() && tokenHadUnsolvedChallenges) {
            SnowCaptcha.getDebugLogger().getLogger().info(Util.sha2Hash(ip.toString()) + " solved all challenges");
            SnowCaptcha.getRedis().getRedisTokenDatabase().updateTokenExpiry(tokenRequest.getSitekey(), token);
            ReputationTester.getInstance().updateReputation(ip);
        }

        ctx.json(tokenResponse);
    }
}
