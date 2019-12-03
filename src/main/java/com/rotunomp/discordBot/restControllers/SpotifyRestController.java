package com.rotunomp.discordBot.restControllers;
import com.rotunomp.discordBot.models.AppUser;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.services.AppSessionService;
import com.rotunomp.discordBot.services.AppUserService;
import com.rotunomp.discordBot.services.SpotifyService;
import com.wrapper.spotify.model_objects.specification.Artist;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

        /*  Search Spotify for a name and return 5 artists
         *
         *   Params: searchString
         *
         *   Request Body Layout: NONE
         *
         *   Response Body Layout:
         *   status: 200
         *   {
         *      [
         *          ...
         *          {
         *              id: 'artistId',
         *              name: 'artistName'
         *          },
         *          ...
         *      ]
         *   }
         */
        get("/searchArtists/:searchString", (request, response) -> {
            List<Artist> spotifyArtists = spotifyService.searchArtistsByName(
                    request.params(":searchString"), 5
            );
            List<FollowedArtist> followedArtists = new ArrayList<>();
            for(Artist spotifyArtist : spotifyArtists) {
                FollowedArtist followedArtist = new FollowedArtist();
                followedArtist.setName(spotifyArtist.getName());
                followedArtist.setId(spotifyArtist.getId());
                followedArtists.add(followedArtist);
            }

            response.status(200);
            return followedArtists;
        }, jsonWithExposeAnnotation());

        /*   Get a user's followed artists
         *
         *   Params: sessionId
         *
         *   Request Body Layout: NONE
         *
         *   Response Body Layout:
         *   status: 200
         *   {
         *      [
         *          ...
         *          {
         *              id: 'artistId',
         *              name: 'artistName'
         *          },
         *          ...
         *      ]
         *   }
         */
        get(
                "/users/follow/:sessionId",
                (request, response) -> {
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            request.params(":sessionId")
                    );

                    response.status(200);
                    return spotifyService.getFollowedArtistsListForDiscordId(discordId);
                },
                jsonWithExposeAnnotation()
        );

        /*   Follow an artist
         *
         *   Params: sessionId, artistId
         *
         *   Request Body Layout: NONE
         *
         *   Response: 201
         */
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

        /*   Delete a artist from a user's follows
         *
         *   Params: sessionId, artistId
         *
         *   Request Body Layout: NONE
         *
         *   Response: 204
         */
        delete(
                "/users/follow/:sessionId/:artistId",
                (request, response) -> {
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            request.params(":sessionId")
                    );

                    spotifyService.unfollowArtist(
                            request.params(":artistId"), discordId
                    );

                    response.status(204);
                    return "";
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

        /*   Follow a user's followed artists on Spotify (assuming they already
         *   singed into Spotify on our app). Returns all their followed artists
         *
         *   Params: sessionId
         *
         *   Request Body Layout: NONE
         *
         *   Response Body Layout:
         *   status: 200
         *   {
         *      [
         *          ...
         *          {
         *              id: 'artistId',
         *              name: 'artistName'
         *          },
         *          ...
         *      ]
         *   }
         */
        post("artists/:sessionId", (request, response) -> {
            String discordId = appSessionService.getDiscordIdFromSessionId(
                    request.params(":sessionId")
            );
            AppUser appUser = appUserService.getAppUserWithDiscordId(discordId);

            // TODO: Surround with try/catch and handle that
            spotifyService.followAllArtistsFollowedOnSpotify(appUser);

            return spotifyService.getFollowedArtistsForDiscordUser(discordId);
        }, jsonWithExposeAnnotation());

    }

    
}
