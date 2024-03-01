package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.Account;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class AccountsRoute {
    public static void get(Context ctx) {
        try {
            List<Account> accounts = SnowCaptcha.getMysql().getMysqlAccountDatabase().getAccounts();

            HashMap<String, Object> model = new HashMap<>();
            model.put("accounts", new ObjectMapper().writeValueAsString(accounts));

            Route.render(ctx, "admin/accounts", model);
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }
}
