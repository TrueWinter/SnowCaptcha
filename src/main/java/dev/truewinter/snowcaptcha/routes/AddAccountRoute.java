package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;

public class AddAccountRoute {
    public static void get(Context ctx) {
        Route.render(ctx, "admin/add-account");
    }

    public static void post(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        String confirmPassword = ctx.formParam("confirm-password");

        if (Util.isBlank(username) || Util.isBlank(password) || Util.isBlank(confirmPassword)) {
            Route.renderError(ctx, "admin/add-account", "All fields are required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Route.renderError(ctx, "admin/add-account", "Passwords must match");
            return;
        }

        try {
            SnowCaptcha.getMysql().getMysqlAccountDatabase().addAccount(username, password);
            ctx.redirect("/admin/accounts");
        } catch (SQLException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
