package dev.truewinter.snowcaptcha.routes;

import io.javalin.http.Context;

public class LogoutRoute {
    public static void get(Context ctx) {
        ctx.removeCookie("session");
        ctx.redirect("/login");
    }
}
