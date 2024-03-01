package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.Widget;
import dev.truewinter.snowcaptcha.database.MysqlWidgetDatabase;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class ResetSecretKeyRoute {
    public static void get(Context ctx) {
        ctx.redirect("/admin/widgets/" + ctx.pathParam("sitekey") + "/edit");
    }

    public static void post(Context ctx) {
        final String sitekey = ctx.pathParam("sitekey");
        final MysqlWidgetDatabase mysqlWidgetDatabase = SnowCaptcha.getMysql().getMysqlWidgetDatabase();
        try {
            Optional<Widget> widget = mysqlWidgetDatabase.getWidget(sitekey);

            if (widget.isEmpty()) {
                ctx.redirect("/admin/widgets");
                return;
            }

            Widget widget1 = mysqlWidgetDatabase.resetSecretKey(sitekey);
            HashMap<String, Object> model = new HashMap<>();
            model.put("widget", JsonMapper.builder()
                    .configure(MapperFeature.USE_ANNOTATIONS, false).build()
                    .writeValueAsString(widget1));
            Route.render(ctx, "admin/edit-widget", model);
        } catch(SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
