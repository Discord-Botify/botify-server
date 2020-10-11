package com.rotunomp.discordBot.restControllers;

import com.rotunomp.discordBot.services.AppSessionService;
import com.rotunomp.discordBot.services.AppUserService;
import com.rotunomp.discordBot.services.DiscordService;
import com.rotunomp.discordBot.services.SpotifyService;
import com.wrapper.spotify.model_objects.specification.User;
import org.json.JSONObject;
import spark.Filter;

import static com.rotunomp.discordBot.app.JsonUtil.json;
import static spark.Spark.*;

public class OauthRestController {
    private SpotifyService spotifyService;
    private DiscordService discordService;
    private AppSessionService appSessionService;
    private AppUserService appUserService;

    public OauthRestController() {
        spotifyService = SpotifyService.getService();
        discordService = DiscordService.getInstance();
        appSessionService = AppSessionService.getInstance();
        appUserService = AppUserService.getInstance();

//        before((request, response) -> {
//            System.out.println("We're in the before block for Spark");
//            response.header("Access-Control-Allow-Origin", "*");
//            response.header("Access-Control-Request-Method", "*");
//            response.header("Access-Control-Allow-Headers", "*");
//            // Note: this may or may not be necessary in your particular application
//            response.type("application/json");
//        });


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


        /*  Spotify oauth:
         *   Receives a code for Spotify oauth. Retrieves the access and refresh tokens
         *   for a Spotify account, and gets the username for display in the app. Saves
         *   the refresh token and username in the database for future use.
         *
         *   Params: NONE
         *
         *   Request Body Layout:
         *   {
         *       code: 'code'
         *       sessionId: 'sessionId'
         *   }
         *
         *   Response Body Layout:
         *   status: 201
         *   {
         *       spotifyUserName: 'name'
         *   }
         */
        post(
                "/oauth/spotify",
                ((request, response) -> {
                    // Get the access tokens from the Spotify API
                    String body = request.body();
                    JSONObject jsonBody = new JSONObject(body);
                    String code = jsonBody.getString("code");
                    JSONObject accessTokenJson = spotifyService.exchangeCodeForTokens(code);

                    System.out.println("Spotify access token response: " + accessTokenJson.toString());

                    // Get the user's Spotify information
                    String accessToken = accessTokenJson.getString("access_token");

                    User user = spotifyService.getUsersSpotifyInfo2(accessToken);

                    // Add refresh token ans Spotify info to the corresponding user object
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            jsonBody.getString("sessionId")
                    );
                    String spotifyId = user.getId();
                    String spotifyUserName = user.getDisplayName();
                    String spotifyRefreshToken = accessTokenJson.getString("refresh_token");

                    appUserService.saveSpotifyInformation(
                            discordId, spotifyId, spotifyUserName, spotifyRefreshToken
                    );

                    // Set the response status and return the appropriate user data
                    response.status(201);
                    return spotifyUserName;
                }),
                json()
        );

        /*  Log a user out of their Spotify, thus deleting their
         *   Spotify account records in the associated AppUser object
         *
         *   Params: sessionId
         *
         *   Request Body Layout: NONE
         *
         *   Response: 204
         */
        delete(
                "/oauth/spotify/:sessionId",
                (request, response) -> {
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            request.params(":sessionId")
                    );
                    appUserService.deleteSpotifyInformation(discordId);

                    response.status(204);
                    return "";
                },
                json()
        );

    }
}
