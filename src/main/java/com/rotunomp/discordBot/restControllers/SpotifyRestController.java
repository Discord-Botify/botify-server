package com.rotunomp.discordBot.restControllers;
import com.rotunomp.discordBot.services.AppSessionService;
import com.rotunomp.discordBot.services.AppUserService;
import com.rotunomp.discordBot.services.SpotifyService;
import org.json.JSONObject;

import static spark.Spark.*;
import static com.rotunomp.discordBot.app.JsonUtil.*;

public class SpotifyRestController {

    private SpotifyService spotifyService;
    private AppSessionService appSessionService;
    private AppUserService appUserService;

    public SpotifyRestController() {
        spotifyService = SpotifyService.getService();
        appSessionService = AppSessionService.getInstance();
        appUserService = AppUserService.getInstance();

        // Get all artists in the database (proof of concept, not very functional)
        get("/artists",
                (request, response) -> spotifyService.getAllDatabaseArtists(),
                jsonWithExposeAnnotation()
        );

        // Get List of followed artists from session ID
        get(
                "/users/follow/:sessionId",
                (request, response) -> {
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            request.params(":sessionId")
                    );

                    return spotifyService.getFollowedArtistsListForDiscordId(discordId);
                },
                jsonWithExposeAnnotation()
        );

        // Follow an artist
        post(
                "/users/follow/:sessionId/:artistId",
                (request, response) -> {
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            request.params(":sessionId")
                    );

                    spotifyService.followArtistById(
                            request.params(":artistId"), discordId
                    );

                    response.status(201);
                    return "FOLLOWED";
                },
                jsonWithExposeAnnotation()
        );

        // Delete a follow
        delete(
                "/users/follow/:sessionId/:artistId",
                (request, response) -> {
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            request.params(":sessionId")
                    );

                    spotifyService.unfollowArtist(
                            request.params(":artistId"), discordId
                    );

                    response.status(200);
                    return "DELETED";
                },
                jsonWithExposeAnnotation()

        );


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

                    // Get the user's Spotify information
                    String accessToken = accessTokenJson.getString("access_token");
                    JSONObject userInfoJson = spotifyService.getUsersSpotifyInfo(accessToken);

                    // Add refresh token ans Spotify info to the corresponding user object
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            jsonBody.getString("sessionId")
                    );
                    String spotifyId = userInfoJson.getString("id");
                    String spotifyUserName = userInfoJson.getString("display_name");
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

    }

    
}
