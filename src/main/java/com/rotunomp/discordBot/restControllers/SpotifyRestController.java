package com.rotunomp.discordBot.restControllers;
import com.rotunomp.discordBot.services.SpotifyService;

import static spark.Spark.*;
import static com.rotunomp.discordBot.app.JsonUtil.*;

public class SpotifyRestController {

    private SpotifyService spotifyService;

    public SpotifyRestController() {
        spotifyService = SpotifyService.getService();

        // Get all artists in the database (proof of concept, not very functional)
        get("/artists",
                (request, response) -> spotifyService.getAllDatabaseArtists(),
                jsonWithExposeAnnotation()
        );

        // Get all a user's followed artists in the database (based on a user's Spotify id)
        get(
                "/users",
                (request, response) -> spotifyService.getFollowedArtistsListForSpotifyId(
                        request.params("spotifyUserId")
                ),
                jsonWithExposeAnnotation()
        );
    }

}
