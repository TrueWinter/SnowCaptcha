package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.RegionChallenge;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaptchasRoute {
    public static void get(Context ctx) {
        try {
            List<RegionChallenge> challenges = SnowCaptcha.getMysql().getMysqlRegionChallengeDatabase()
                    .getRegionChallenges();

            List<Map<String, Object>> views = new ArrayList<>();
            for (RegionChallenge challenge : challenges) {
                views.add(RegionChallengeUtil.toViewModel(challenge));
            }

            HashMap<String, Object> model = new HashMap<>();
            model.put("challenges", new ObjectMapper().writeValueAsString(views));

            Route.render(ctx, "admin/captchas", model);
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
