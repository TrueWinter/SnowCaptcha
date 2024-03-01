package dev.truewinter.snowcaptcha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import dev.truewinter.snowcaptcha.admin.InvalidCsrfTokenException;
import dev.truewinter.snowcaptcha.admin.NotLoggedInException;
import dev.truewinter.snowcaptcha.config.DevModeConfig;
import dev.truewinter.snowcaptcha.config.HttpConfig;
import dev.truewinter.snowcaptcha.config.HttpIpConfig;
import dev.truewinter.snowcaptcha.pebble.PebbleExtension;
import dev.truewinter.snowcaptcha.routes.*;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinPebble;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WebServer extends Thread {
    private final HttpConfig httpConfig;
    private final DevModeConfig devModeConfig;
    private final HttpIpConfig httpIpConfig;
    private Javalin server;

    protected WebServer(HttpConfig httpConfig, DevModeConfig devModeConfig) {
        this.httpConfig = httpConfig;
        this.devModeConfig = devModeConfig;
        this.httpIpConfig = httpConfig.getHttpIpConfig();
    }

    @Override
    public void run() {
        PebbleEngine.Builder pebbleEngine = new PebbleEngine.Builder();
        Loader<String> loader;

        if (devModeConfig.isEnabled()) {
            loader = new FileLoader();
            loader.setPrefix("src/main/resources/web/templates");
            pebbleEngine.cacheActive(false)
                    .templateCache(null)
                    .tagCache(null);
        } else {
            loader = new ClasspathLoader();
            loader.setPrefix("web/templates");
            loader.setSuffix(".peb");
        }

        loader.setSuffix(".peb");
        pebbleEngine.loader(loader);
        pebbleEngine.extension(new PebbleExtension());

        server = Javalin.create(c -> {
            c.showJavalinBanner = false;
            c.staticFiles.add(s -> {
                s.directory = "web/public";
                if (devModeConfig.isEnabled()) {
                    s.directory = "src/main/resources/" + s.directory;
                    s.location = Location.EXTERNAL;
                } else {
                    s.location = Location.CLASSPATH;
                }
            });
            c.fileRenderer(new JavalinPebble(pebbleEngine.build()));
        }).start(httpConfig.getPort());

        server.before(ctx -> {
            ctx.header("X-Robots-Tag", "noindex");

            if (ctx.path().startsWith("/build/captcha")) {
                ctx.header(Header.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
                ctx.header(Header.EXPIRES, "0");
            }
        });

        loadAssetManifest();
        if (devModeConfig.isEnabled()) {
            server.get("/_dev/reload", ctx -> {
                loadAssetManifest();
                ctx.result("OK");
            });
        }

        if (devModeConfig.isTestPageEnabled()) {
            server.get("/captcha/test", ctx -> {
                Map<String, String> data = new HashMap<>();
                data.put("sitekey", devModeConfig.getSitekey());
                data.put("secretkey", devModeConfig.getSecretkey());
                data.put("host", devModeConfig.getHost());
                data.put("src", devModeConfig.getSrc());
                ctx.render("test", data);
            });
        }

        server.get("/privacy", ctx -> {
            if (!Util.isBlank(httpConfig.getPrivacyRedirect())) {
                ctx.redirect(httpConfig.getPrivacyRedirect(), HttpStatus.FOUND);
                return;
            }

            ctx.render("privacy");
        });

        server.get("/health", ctx -> {
            ctx.result("OK");
        });

        // Doing this before the POST handler allows the CORS header to still be set if the handler errors
        GetTokenRoute.init(httpIpConfig, devModeConfig);
        server.before("/get-token", GetTokenRoute::before);
        server.post("/get-token", GetTokenRoute::post);

        server.post("/validate-sitekey", ValidateSitekeyRoute::post);
        server.post("/validate-token", ValidateTokenRoute::post);

        server.exception(NotLoggedInException.class, (e, ctx) -> {
            if (ctx.method().equals(HandlerType.GET)) {
                ctx.redirect("/login?redirect=" + URLEncoder.encode(ctx.path(), StandardCharsets.UTF_8));
            } else {
                ctx.redirect("/login");
            }
        });
        server.exception(InvalidCsrfTokenException.class, (e, ctx) -> {
            ctx.status(400).result("Invalid CSRF token");
        });
        server.before("/admin/*", ctx -> {
            if (!Route.isLoggedIn(ctx)) {
                throw new NotLoggedInException();
            }

            if (ctx.method() == HandlerType.POST && !Route.isCsrfTokenValid(ctx)) {
                throw new InvalidCsrfTokenException();
            }
        });

        server.get("/", ctx -> {
            ctx.redirect("/admin/widgets");
        });

        server.get("/admin", ctx -> {
            ctx.redirect("/admin/widgets");
        });

        server.get("/login", LoginRoute::get);
        server.post("/login", LoginRoute::post);
        server.get("/logout", LogoutRoute::get);
        server.get("/admin/widgets", WidgetsRoute::get);
        server.get("/admin/accounts", AccountsRoute::get);
        server.get("/admin/accounts/{username}/edit", EditAccountRoute::get);
        server.post("/admin/accounts/{username}/edit", EditAccountRoute::post);
        server.post("/admin/accounts/{username}/delete", DeleteAccountRoute::post);
        server.get("/admin/accounts/add", AddAccountRoute::get);
        server.post("/admin/accounts/add", AddAccountRoute::post);
        server.get("/admin/widgets/add", AddWidgetRoute::get);
        server.post("/admin/widgets/add", AddWidgetRoute::post);
        server.get("/admin/widgets/{sitekey}/edit", EditWidgetRoute::get);
        server.post("/admin/widgets/{sitekey}/edit", EditWidgetRoute::post);
        server.get("/admin/widgets/{sitekey}/reset", ResetSecretKeyRoute::get);
        server.post("/admin/widgets/{sitekey}/reset", ResetSecretKeyRoute::post);
        server.post("/admin/widgets/{sitekey}/delete", DeleteWidgetRoute::post);
    }

    public void shutdown() {
        if (server != null) {
            server.stop();
        }
    }

    private void loadAssetManifest() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            @SuppressWarnings("unchecked")
            Map<String, String> json = objectMapper.readValue(devModeConfig.isEnabled() ?
                            new FileInputStream(new File("src/main/resources/web/public/build/assets-manifest.json")) :
                            getClass().getClassLoader().getResourceAsStream("web/public/build/assets-manifest.json"),
                    Map.class);
            Route.setAssetManifest(json);
        } catch (Exception e) {
            SnowCaptcha.getLogger().error("Failed to load asset manifest", e);
        }
    }
}
