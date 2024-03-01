package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.admin.LoginCookie;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.Account;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.Optional;

public class LoginRoute {
    public static void get(Context ctx) {
        if (Route.isLoggedIn(ctx)) {
            ctx.redirect("/admin");
            return;
        }

        Route.render(ctx, "admin/login");
    }

    public static void post(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        if (Util.isBlank(username) || Util.isBlank(password)) {
            Route.renderError(ctx, "admin/login", "Username or password invalid");
            return;
        }

        try {
            Optional<Account> account = SnowCaptcha.getMysql().getMysqlAccountDatabase().getAccount(username);

            if (account.isEmpty() || !Account.isCorrectPassword(password, account.get())) {
                Route.renderError(ctx, "admin/login", "Username or password invalid");
                return;
            }

            LoginCookie loginCookie = new LoginCookie(username);
            ctx.cookie("session", loginCookie.getJWT());

            String redirect = ctx.queryParam("redirect");
            if (Util.isBlank(redirect)) {
                redirect = "/admin";
            }
            ctx.redirect(redirect);
        } catch (SQLException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
