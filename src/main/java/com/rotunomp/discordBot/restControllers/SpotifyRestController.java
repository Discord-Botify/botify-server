package com.rotunomp.discordBot.restControllers;
import com.google.gson.Gson;
import com.rotunomp.discordBot.app.StandardResponse;
import com.rotunomp.discordBot.app.StatusResponse;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.services.SpotifyService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

import static spark.Spark.*;
import static com.rotunomp.discordBot.app.JsonUtil.*;

public class SpotifyRestController {

    private SpotifyService spotifyService;

    public SpotifyRestController() {
        spotifyService = SpotifyService.getService();

        get("/artists", new Route() {
            @Override
            public List<FollowedArtist> handle(Request request, Response response) throws Exception {
                return spotifyService.getAllDatabaseArtists();
            }
        }, json());
    }

}
