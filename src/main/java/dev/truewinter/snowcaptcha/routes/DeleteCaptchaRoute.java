package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.RegionChallenge;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.Optional;

public class DeleteCaptchaRoute {
    public static void post(Context ctx) {
        try {
            long id;
            try {
                id = Long.parseLong(ctx.pathParam("id"));
            } catch (NumberFormatException e) {
                ctx.redirect("/admin/captchas");
                return;
            }

            var database = SnowCaptcha.getMysql().getMysqlRegionChallengeDatabase();
            Optional<RegionChallenge> challenge = database.getRegionChallenge(id);

            if (challenge.isEmpty()) {
                ctx.redirect("/admin/captchas");
                return;
            }

            database.deleteRegionChallenge(id);
            RegionChallengeUtil.deleteImageQuietly(challenge.get().getObjectKey());

            ctx.redirect("/admin/captchas");
        } catch (SQLException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
