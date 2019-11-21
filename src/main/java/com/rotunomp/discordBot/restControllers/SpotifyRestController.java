package com.rotunomp.discordBot.restControllers;
import com.google.gson.Gson;
import com.rotunomp.discordBot.app.Properties;
import com.rotunomp.discordBot.services.AppSessionService;
import com.rotunomp.discordBot.services.SpotifyService;

import static spark.Spark.*;
import static com.rotunomp.discordBot.app.JsonUtil.*;

public class SpotifyRestController {

    private SpotifyService spotifyService;
    private AppSessionService appSessionService;

    public SpotifyRestController() {
        spotifyService = SpotifyService.getService();
        appSessionService = AppSessionService.getInstance();

        // Get all artists in the database (proof of concept, not very functional)
        get("/artists",
                (request, response) -> spotifyService.getAllDatabaseArtists(),
                jsonWithExposeAnnotation()
        );

        // Get discord ID from session ID
        get(
                "/users/:sessionId",
                (request, response) -> {
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            request.params(":sessionId")
                    );

                    return spotifyService.getFollowedArtistsListForDiscordId(discordId);
                },
                jsonWithExposeAnnotation()
        );

        post(
                "/users/follow/:sessionId/:artistId",
                (request, response) -> {
                    String discordId = appSessionService.getDiscordIdFromSessionId(
                            request.params(":sessionId")
                    );

                    spotifyService.followArtist(
                            request.params(":artistId"), discordId
                    );

                    response.status(201);
                    return "FOLLOWED";
                },
                jsonWithExposeAnnotation()
        );

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

    }

    
}
