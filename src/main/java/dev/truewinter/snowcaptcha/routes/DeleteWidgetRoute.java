package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.Widget;
import dev.truewinter.snowcaptcha.database.MysqlWidgetDatabase;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.Optional;

public class DeleteWidgetRoute {
    public static void post(Context ctx) {
        final MysqlWidgetDatabase mysqlWidgetDatabase = SnowCaptcha.getMysql().getMysqlWidgetDatabase();
        final String sitekey = ctx.pathParam("sitekey");
        try {
            Optional<Widget> widget = mysqlWidgetDatabase.getWidget(sitekey);

            if (widget.isEmpty()) {
                ctx.redirect("/admin/widgets");
                return;
            }

            mysqlWidgetDatabase.deleteWidget(sitekey);
            ctx.redirect("/admin/widgets");
        } catch (SQLException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
