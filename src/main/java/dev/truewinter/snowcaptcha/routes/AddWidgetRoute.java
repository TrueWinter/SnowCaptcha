package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.Widget;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.HashMap;

public class AddWidgetRoute {
    public static void get(Context ctx) {
        Route.render(ctx, "admin/add-widget");
    }

    public static void post(Context ctx) {
        String name = ctx.formParam("name");

        if (Util.isBlank(name)) {
            Route.renderError(ctx, "admin/add-widget", "All fields are required");
            return;
        }

        try {
            Widget widget = SnowCaptcha.getMysql().getMysqlWidgetDatabase().addWidget(name);
            HashMap<String, Object> model = new HashMap<>();
            model.put("widget", JsonMapper.builder()
                    .configure(MapperFeature.USE_ANNOTATIONS, false).build()
                    .writeValueAsString(widget));
            Route.render(ctx, "admin/edit-widget", model);
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
