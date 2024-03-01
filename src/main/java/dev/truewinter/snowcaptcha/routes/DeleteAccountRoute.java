package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.Account;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.Optional;

public class DeleteAccountRoute {
    public static void post(Context ctx) {
        try {
            Optional<Account> account = SnowCaptcha.getMysql().getMysqlAccountDatabase()
                    .getAccount(ctx.pathParam("username"));

            if (account.isPresent()) {
                SnowCaptcha.getMysql().getMysqlAccountDatabase().deleteAccount(ctx.pathParam("username"));
            }
            ctx.redirect("/admin/accounts");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
