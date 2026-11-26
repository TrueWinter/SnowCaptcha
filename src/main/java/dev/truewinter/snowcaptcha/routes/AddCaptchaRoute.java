package dev.truewinter.snowcaptcha.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.admin.RegionChallenge;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;

import java.util.HashMap;

public class AddCaptchaRoute {
    public static void get(Context ctx) {
        HashMap<String, Object> model = new HashMap<>();
        if (SnowCaptcha.getS3() == null) {
            model.put("error", "S3 is not configured, region challenge images cannot be uploaded");
        }
        Route.render(ctx, "admin/add-captcha", model);
    }

    public static void post(Context ctx) {
        RegionChallengeUtil.UploadedImage image = null;
        boolean writeAttempted = false;

        try {
            RegionChallengeUtil.FormData form = RegionChallengeUtil.parseForm(ctx);
            image = RegionChallengeUtil.uploadImage(ctx.uploadedFile("image"));

            writeAttempted = true;
            RegionChallenge regionChallenge = SnowCaptcha.getMysql().getMysqlRegionChallengeDatabase()
                    .addRegionChallenge(image.objectKey(), image.contentType(), form.instructions(),
                            form.x(), form.y(), form.width(), form.height(),
                            image.width(), image.height());

            HashMap<String, Object> model = new HashMap<>();
            model.put("challenge", new ObjectMapper().writeValueAsString(
                    RegionChallengeUtil.toViewModel(regionChallenge)));

            Route.renderSuccess(ctx, "admin/edit-captcha", model, "Added captcha");
        } catch (IllegalArgumentException | IllegalStateException e) {
            RegionChallengeUtil.cleanupUploadedImage(image != null ? image.objectKey() : null, writeAttempted);
            Route.renderError(ctx, "admin/add-captcha", e.getMessage());
        } catch (Exception e) {
            RegionChallengeUtil.cleanupUploadedImage(image != null ? image.objectKey() : null, writeAttempted);
            SnowCaptcha.getLogger().error("Failed to add region challenge", e);
            throw new InternalServerErrorResponse();
        }
    }
}
