package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.Widget;
import dev.truewinter.snowcaptcha.challenge.SerializedChallenge;
import dev.truewinter.snowcaptcha.token.TokenNotFoundException;
import dev.truewinter.snowcaptcha.token.TokenValidationRequest;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Optional;

public class ValidateTokenRoute {
    public static void post(Context ctx) throws Exception {
        TokenValidationRequest tokenValidationRequest;
        try {
            tokenValidationRequest = ctx.bodyAsClass(TokenValidationRequest.class);

            if (Util.isBlank(tokenValidationRequest.getToken()) || Util.isBlank(tokenValidationRequest.getSitekey()) ||
                Util.isBlank(tokenValidationRequest.getSecretkey())) {
                throw new BadRequestResponse();
            }
        } catch (Exception e) {
            if (e.getCause() instanceof TokenNotFoundException) {
                error(ctx);
                return;
            }
            error(ctx, 400);
            return;
        }

        Optional<Widget> widget = SnowCaptcha.getMysql().getMysqlWidgetDatabase()
                .getWidget(tokenValidationRequest.getSitekey());
        if (widget.isEmpty() || !Widget.isCorrectKey(tokenValidationRequest.getSecretkey(), widget.get())) {
            error(ctx, 403);
            return;
        }

        HashMap<String, SerializedChallenge> challenges = SnowCaptcha.getRedis().getRedisTokenDatabase()
                .getTokenData(tokenValidationRequest.getSitekey(), tokenValidationRequest.getToken())
                .orElse(null);

        if (challenges == null || !challenges.isEmpty()) {
            error(ctx);
            return;
        }

        HashMap<String, Object> resp = new HashMap<>();
        resp.put("valid", true);
        ctx.json(resp);

        SnowCaptcha.getRedis().getRedisTokenDatabase().removeKey(tokenValidationRequest.getSitekey(),
                tokenValidationRequest.getToken());
    }

    private static void error(Context ctx) {
        error(ctx, 200);
    }

    private static void error(Context ctx, int status) {
        HashMap<String, Object> resp = new HashMap<>();
        resp.put("valid", false);
        ctx.status(status).json(resp);
    }
}
