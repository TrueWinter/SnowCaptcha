package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.RegionChallenge;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class EditCaptchaRoute {
    public static void get(Context ctx) {
        try {
            Optional<RegionChallenge> challenge = getChallenge(ctx);
            if (challenge.isEmpty()) {
                ctx.redirect("/admin/captchas");
                return;
            }

            HashMap<String, Object> model = new HashMap<>();
            model.put("challenge", new ObjectMapper().writeValueAsString(
                    RegionChallengeUtil.toViewModel(challenge.get())));

            Route.render(ctx, "admin/edit-captcha", model);
        } catch (SQLException | JsonProcessingException e) {
            throw new InternalServerErrorResponse();
        }
    }

    public static void post(Context ctx) {
        RegionChallengeUtil.UploadedImage newImage = null;
        boolean writeAttempted = false;

        try {
            Optional<RegionChallenge> existing = getChallenge(ctx);
            if (existing.isEmpty()) {
                ctx.redirect("/admin/captchas");
                return;
            }

            RegionChallengeUtil.FormData form = RegionChallengeUtil.parseForm(ctx);

            int expectedVersion;
            try {
                expectedVersion = Integer.parseInt(ctx.formParam("version"));
            } catch (NumberFormatException e) {
                renderConflict(ctx);
                return;
            }

            var file = ctx.uploadedFile("image");
            String objectKey = existing.get().getObjectKey();
            String contentType = existing.get().getContentType();

            if (file != null && file.size() > 0) {
                newImage = RegionChallengeUtil.uploadImage(file);
                objectKey = newImage.objectKey();
                contentType = newImage.contentType();
            }

            var database = SnowCaptcha.getMysql().getMysqlRegionChallengeDatabase();
            writeAttempted = true;
            int updatedRows = database.updateRegionChallenge(existing.get().getId(),
                    expectedVersion, objectKey, contentType,
                    form.instructions(), form.x(), form.y(), form.width(), form.height());

            if (updatedRows == 0) {
                if (newImage != null) {
                    RegionChallengeUtil.deleteImageQuietly(newImage.objectKey());
                }

                renderConflict(ctx);
                return;
            }

            if (newImage != null) {
                RegionChallengeUtil.deleteImageQuietly(existing.get().getObjectKey());
            }

            Optional<RegionChallenge> updated = getChallenge(ctx);
            if (updated.isEmpty()) {
                ctx.redirect("/admin/captchas");
                return;
            }

            HashMap<String, Object> model = new HashMap<>();
            model.put("challenge", new ObjectMapper().writeValueAsString(
                    RegionChallengeUtil.toViewModel(updated.get())));

            Route.renderSuccess(ctx, "admin/edit-captcha", model, "Saved captcha");
        } catch (IllegalArgumentException | IllegalStateException e) {
            RegionChallengeUtil.cleanupUploadedImage(
                    newImage != null ? newImage.objectKey() : null, writeAttempted);
            HashMap<String, Object> model = new HashMap<>();
            try {
                Optional<RegionChallenge> challenge = getChallenge(ctx);
                if (challenge.isPresent()) {
                    model.put("challenge", writeChallenge(challenge.get()));
                }
            } catch (SQLException | JsonProcessingException ignored) {
            }
            Route.renderError(ctx, "admin/edit-captcha", model, e.getMessage());
        } catch (Exception e) {
            RegionChallengeUtil.cleanupUploadedImage(
                    newImage != null ? newImage.objectKey() : null, writeAttempted);
            SnowCaptcha.getLogger().error("Failed to edit region challenge", e);
            throw new InternalServerErrorResponse();
        }
    }

    private static void renderConflict(Context ctx) {
        HashMap<String, Object> model = new HashMap<>();
        try {
            Optional<RegionChallenge> challenge = getChallenge(ctx);
            if (challenge.isPresent()) {
                model.put("challenge", writeChallenge(challenge.get()));
            }
        } catch (SQLException | JsonProcessingException ignored) {
        }

        Route.renderError(ctx, "admin/edit-captcha", model,
                "This captcha was changed by another request. Review it and try again.");
    }

    private static Optional<RegionChallenge> getChallenge(Context ctx) throws SQLException {
        long id;
        try {
            id = Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        return SnowCaptcha.getMysql().getMysqlRegionChallengeDatabase().getRegionChallenge(id);
    }

    private static String writeChallenge(RegionChallenge challenge) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(RegionChallengeUtil.toViewModel(challenge));
    }
}
