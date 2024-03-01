package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.Widget;
import dev.truewinter.snowcaptcha.database.MysqlWidgetDatabase;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class EditWidgetRoute {
    public static void get(Context ctx) {
        try {
            Optional<Widget> widget = SnowCaptcha.getMysql().getMysqlWidgetDatabase()
                    .getWidget(ctx.pathParam("sitekey"));

            if (widget.isEmpty()) {
                ctx.redirect("/admin/widgets");
                return;
            }

            HashMap<String, Object> model = new HashMap<>();
            model.put("widget", new ObjectMapper().writeValueAsString(widget.get()));

            Route.render(ctx, "admin/edit-widget", model);
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }

    public static void post(Context ctx) {
        final MysqlWidgetDatabase mysqlWidgetDatabase = SnowCaptcha.getMysql().getMysqlWidgetDatabase();
        final String sitekey = ctx.pathParam("sitekey");
        try {
            Optional<Widget> widget = mysqlWidgetDatabase.getWidget(sitekey);

            if (widget.isEmpty()) {
                ctx.redirect("/admin/widgets");
                return;
            }

            String name = ctx.formParam("name");
            if (Util.isBlank(name)) {
                Route.renderError(ctx, "admin/edit-widget", "All fields are required");
                return;
            }

            mysqlWidgetDatabase.changeName(sitekey, name);

            Optional<Widget> newWidget = mysqlWidgetDatabase.getWidget(sitekey);
            if (newWidget.isEmpty()) {
                ctx.redirect("/admin/widgets");
                return;
            }
            HashMap<String, Object> model = new HashMap<>();
            model.put("widget", new ObjectMapper().writeValueAsString(newWidget.get()));
            Route.renderSuccess(ctx, "admin/edit-widget", model, "Renamed widget");
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
