package com.rotunomp.discordBot.restControllers;

import com.rotunomp.discordBot.models.AppUser;
import com.rotunomp.discordBot.services.AppSessionService;
import com.rotunomp.discordBot.services.AppUserService;
import com.rotunomp.discordBot.services.DiscordService;
import org.json.JSONObject;

import static com.rotunomp.discordBot.app.JsonUtil.json;
import static spark.Spark.*;

/**
 * DiscordRestController
 */
public class DiscordRestController {
    private DiscordService discordService;
    private AppSessionService appSessionService;
    private AppUserService appUserService;

    public DiscordRestController() {
        discordService = DiscordService.getInstance();
        appSessionService = AppSessionService.getInstance();
        appUserService = AppUserService.getInstance();

        /*  Part of the Discord oauth process, exchanges code for
         *  access tokens, start an AppSession, and return the
         *  session ID and Discord user info
         *
         *  Params: sessionId
         *
         *  Request Body Layout:
         *  {
         *      code: 'code'
         *  }
         *
         *  Response Body Layout:
         *  status: 201
         *  {
         *      appSessionId: 'id',
         *      discordName: 'name',
         *      discordDiscriminator: 'discriminator'
         *  }
         */
        post("/oauth/discord", (request, response) -> {

            // Login the user through Discord OAuth and get their Discord user info
            // Also create them in the database if they don't exist
            String body = request.body();
            JSONObject jsonBody = new JSONObject(body);
            String code = jsonBody.getString("code");
            JSONObject returnJson = discordService.loginUser(code);

            // Start a session for the user
            String discordId = returnJson.getString("id");
            String appSessionId = appSessionService.startAppSession(discordId);

            // Set status to 201
            response.status(201);

            // Build the object we're sending to the front end
            UserInfoTransferObject userInfo = new UserInfoTransferObject();
            userInfo.setAppSessionId(appSessionId);
            userInfo.setDiscordName(returnJson.getString("username"));
            userInfo.setDiscordDiscriminator(returnJson.getString("discriminator"));

            return userInfo;
        }, json());


        /*  Log a user out of their AppSession
         *
         *  Params: sessionId
         *
         *  Request Body Layout: NONE
         *
         *  Response: 204
         */
        delete("/oauth/discord/:sessionId", (request, response) -> {
            appSessionService.deleteAppSession(request.params(":sessionId"));
            response.status(204);
            return "";
        }, json());


        /*  Exchange an existing sessionId for that user's information
         *
         *  Params: sessionId
         *
         *  Request Body Layout: NONE
         *
         *  Response Body Layout:
         *  status: 200
         *  {
         *      appSessionId: 'id',
         *      discordName: 'name',
         *      discordDiscriminator: 'discriminator'
         *
         *  }
         */
        get("/users/:sessionId", (request, response) -> {

            // Get the corresponding AppUser object
            // If the sessionId doesn't exist in the database, respond with a 401
            // so that the calling app knows the session expired
            String discordId = appSessionService.getDiscordIdFromSessionId(
                    request.params(":sessionId")
            );
            AppUser user = appUserService.getAppUserWithDiscordId(discordId);

            UserInfoTransferObject userInfo = new UserInfoTransferObject();
            userInfo.setSpotifyUserName(user.getSpotifyUserName());

            response.status(200);
            return userInfo;
        }, json());

    }


}
