package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.Widget;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class WidgetsRoute {
    public static void get(Context ctx) {
        try {
            List<Widget> widgets = SnowCaptcha.getMysql().getMysqlWidgetDatabase().getWidgets();

            HashMap<String, Object> model = new HashMap<>();
            model.put("widgets", new ObjectMapper().writeValueAsString(widgets));

            Route.render(ctx, "admin/widgets", model);
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
