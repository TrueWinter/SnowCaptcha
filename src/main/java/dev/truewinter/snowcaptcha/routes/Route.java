package dev.truewinter.snowcaptcha.routes;

import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.admin.LoginCookie;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Route {
    private static Map<String, String> assetManifest = new HashMap<>();

    public static boolean isLoggedIn(Context ctx) {
        return getLoginCookie(ctx).isPresent();
    }

    public static void render(Context ctx, String view) {
        render(ctx, view, new HashMap<>());
    }

    public static void render(Context ctx, String view, HashMap<String, Object> model) {
        addDefaultModelValues(ctx, view, model);
        ctx.render(view, model);
    }

    public static void renderError(Context ctx, String view, String error) {
        renderError(ctx, view, new HashMap<>(), error);
    }

    public static void renderError(Context ctx, String view, HashMap<String, Object> model, String error) {
        model.put("error", error);
        render(ctx, view, model);
    }

    public static void renderSuccess(Context ctx, String view, String success) {
        renderError(ctx, view, new HashMap<>(), success);
    }

    public static void renderSuccess(Context ctx, String view, HashMap<String, Object> model, String success) {
        model.put("success", success);
        render(ctx, view, model);
    }

    public static boolean isCsrfTokenValid(Context ctx) {
        String csrf = ctx.formParam("csrf");
        Optional<LoginCookie> loginCookie = getLoginCookie(ctx);
        return loginCookie.filter(cookie -> !Util.isBlank(csrf) && cookie.getCsrf().equals(csrf)).isPresent();
    }

    private static Optional<LoginCookie> getLoginCookie(Context ctx) {
        String cookie = ctx.cookie("session");
        if (cookie == null || cookie.isBlank()) return Optional.empty();

        try {
            return Optional.of(LoginCookie.jwtToLoginCookie(cookie));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static void addDefaultModelValues(Context ctx, String view, HashMap<String, Object> model) {
        Optional<LoginCookie> cookie = getLoginCookie(ctx);
        if (cookie.isPresent()) {
            LoginCookie loginCookie = cookie.get();
            model.put("username", loginCookie.getUsername());
            model.put("csrf", loginCookie.getCsrf());
        }

        model.put("assets", assetManifest);
        model.put("view", view);
    }

    public static void setAssetManifest(Map<String, String> assetManifest) {
        Route.assetManifest = assetManifest;
    }
}
