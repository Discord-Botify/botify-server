package com.rotunomp.discordBot.restControllers;

import com.rotunomp.discordBot.models.AppUser;
import com.rotunomp.discordBot.services.AppSessionService;
import com.rotunomp.discordBot.services.AppUserService;
import com.rotunomp.discordBot.services.DiscordService;
import com.rotunomp.discordBot.services.SpotifyService;

import static com.rotunomp.discordBot.app.JsonUtil.json;
import static com.rotunomp.discordBot.app.JsonUtil.jsonWithExposeAnnotation;
import static spark.Spark.*;

public class AppRestController {
    private DiscordService discordService;
    private AppSessionService appSessionService;
    private AppUserService appUserService;
    private SpotifyService spotifyService;


    public AppRestController() {
        spotifyService = SpotifyService.getService();
        discordService = DiscordService.getInstance();
        appSessionService = AppSessionService.getInstance();
        appUserService = AppUserService.getInstance();


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


        /*   Follow a user's followed artists on Spotify (assuming they already
         *   singed into Spotify on our app). Returns all their followed artists
         *
         *   Params: sessionId
         *
         *   Request Body Layout: NONE
         *
         *   Response Body Layout:
         *   status: 201
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
        post("users/follow/:sessionId", (request, response) -> {
            String discordId = appSessionService.getDiscordIdFromSessionId(
                    request.params(":sessionId")
            );
            AppUser appUser = appUserService.getAppUserWithDiscordId(discordId);

            // TODO: Surround with try/catch and handle that
            spotifyService.followAllArtistsFollowedOnSpotify(appUser);

            response.status(201);
            return spotifyService.getFollowedArtistsListForDiscordId(discordId);
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
        get("/users/follow/:sessionId", (request, response) -> {
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
        post("/users/follow/:sessionId/:artistId", (request, response) -> {
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
        delete("/users/follow/:sessionId/:artistId", (request, response) -> {
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

    }
}
