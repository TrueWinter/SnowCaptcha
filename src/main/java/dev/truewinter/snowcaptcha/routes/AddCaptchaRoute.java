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
        boolean insertedInDatabase = false;

        try {
            RegionChallengeUtil.FormData form = RegionChallengeUtil.parseForm(ctx);
            image = RegionChallengeUtil.uploadImage(ctx.uploadedFile("image"));

            RegionChallenge regionChallenge = SnowCaptcha.getMysql().getMysqlRegionChallengeDatabase()
                    .addRegionChallenge(image.objectKey(), image.contentType(), form.instructions(),
                            form.x(), form.y(), form.width(), form.height());
            insertedInDatabase = true;

            HashMap<String, Object> model = new HashMap<>();
            model.put("challenge", new ObjectMapper().writeValueAsString(
                    RegionChallengeUtil.toViewModel(regionChallenge)));

            Route.renderSuccess(ctx, "admin/edit-captcha", model, "Added captcha");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (image != null && !insertedInDatabase) {
                RegionChallengeUtil.deleteImageQuietly(image.objectKey());
            }
            Route.renderError(ctx, "admin/add-captcha", e.getMessage());
        } catch (Exception e) {
            if (image != null && !insertedInDatabase) {
                RegionChallengeUtil.deleteImageQuietly(image.objectKey());
            }
            SnowCaptcha.getLogger().error("Failed to add region challenge", e);
            throw new InternalServerErrorResponse();
        }
    }
}
