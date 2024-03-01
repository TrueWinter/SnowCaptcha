package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.Widget;
import io.javalin.http.Context;
import org.json.JSONException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class ValidateSitekeyRoute {
    public static void post(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");

        try {
            HashMap<String, String> body = new ObjectMapper().readValue(ctx.body(),
                    TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class));
            String sitekey = body.get("sitekey");
            if (Util.isBlank(sitekey)) {
                ctx.status(400);
                return;
            }

            SnowCaptcha.getMysql().getMysqlWidgetDatabase().getWidget(sitekey)
                    .ifPresentOrElse(w -> ctx.status(200), () -> ctx.status(404));
        } catch (JSONException | JsonProcessingException e) {
            ctx.status(400);
        } catch (SQLException e) {
            ctx.status(500);
        }
    }
}
