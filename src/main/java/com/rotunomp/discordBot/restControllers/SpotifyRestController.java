package com.rotunomp.discordBot.restControllers;
import com.google.gson.Gson;
import com.rotunomp.discordBot.app.Properties;
import com.rotunomp.discordBot.services.SpotifyService;

import static spark.Spark.*;
import static com.rotunomp.discordBot.app.JsonUtil.*;

public class SpotifyRestController {

    private SpotifyService spotifyService;

    public SpotifyRestController() {
        spotifyService = SpotifyService.getService();
        String code;

        // Set up SSL
        String keyStoreLocation = Properties.get("ssl_certificate_location");
        String keyStorePassword = Properties.get("ssl_password");
        secure(keyStoreLocation, keyStorePassword, null, null);

        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));


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
