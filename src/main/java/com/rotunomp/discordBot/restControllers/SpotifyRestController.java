package com.rotunomp.discordBot.restControllers;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.services.SpotifyService;
import com.wrapper.spotify.model_objects.specification.Artist;
import spark.Filter;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;
import static com.rotunomp.discordBot.app.JsonUtil.*;

public class SpotifyRestController {

    private SpotifyService spotifyService;

    public SpotifyRestController() {
        spotifyService = SpotifyService.getService();
        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });

        after((Filter) (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });


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
        get("/artists/search/:searchString", (request, response) -> {
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

    }

    
}
