package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.Account;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class EditAccountRoute {
    public static void get(Context ctx) {
        try {
            Optional<Account> account = SnowCaptcha.getMysql().getMysqlAccountDatabase()
                    .getAccount(ctx.pathParam("username"));

            if (account.isEmpty()) {
                ctx.redirect("/admin/accounts");
                return;
            }

            HashMap<String, Object> model = new HashMap<>();
            model.put("account", new ObjectMapper().writeValueAsString(account.get()));

            Route.render(ctx, "admin/edit-account", model);
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }

    public static void post(Context ctx) {
        try {
            Optional<Account> account = SnowCaptcha.getMysql().getMysqlAccountDatabase()
                    .getAccount(ctx.pathParam("username"));

            if (account.isEmpty()) {
                ctx.redirect("/admin/accounts");
                return;
            }

            String password = ctx.formParam("password");
            String confirmPassword = ctx.formParam("confirm-password");

            // If both are blank, the user doesn't want to change their password
            if (Util.isBlank(password) && Util.isBlank(confirmPassword)) {
                ctx.redirect(ctx.path());
                return;
            }

            // Due to the previous check, this statement will run if only one of the fields are blank
            if (Util.isBlank(password) || Util.isBlank(confirmPassword)) {
                renderPasswordError(ctx, account.get());
                return;
            }

            if (!password.equals(confirmPassword)) {
                renderPasswordError(ctx, account.get());
                return;
            }

            SnowCaptcha.getMysql().getMysqlAccountDatabase().changePassword(ctx.pathParam("username"), password);
            renderSuccess(ctx, account.get());
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }

    private static void renderPasswordError(Context ctx, Account account) throws JsonProcessingException {
        HashMap<String, Object> model = new HashMap<>();
        model.put("account", new ObjectMapper().writeValueAsString(account));

        Route.renderError(ctx, "admin/edit-account", model, "Passwords must match");
    }

    private static void renderSuccess(Context ctx, Account account) throws JsonProcessingException {
        HashMap<String, Object> model = new HashMap<>();
        model.put("account", new ObjectMapper().writeValueAsString(account));

        Route.renderSuccess(ctx, "admin/edit-account", model, "Password changed");
    }
}
