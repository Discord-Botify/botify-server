package com.rotunomp.discordBot.restControllers;

import com.rotunomp.discordBot.app.Properties;
import spark.Spark;

import static spark.Spark.*;

public class RestController {

    public RestController() {
        // Set up SSL
        String keyStoreLocation = Properties.get("ssl_certificate_location");
        String keyStorePassword = Properties.get("ssl_password");
        secure(keyStoreLocation, keyStorePassword, null, null);

        // Enable CORS for all endpoints
        Spark.staticFiles.location("/assets");
        Spark.staticFiles.header("Access-Control-Allow-Origin", "*");

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

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Headers", "*");
            response.type("application/json");
        });

        new SpotifyRestController();
        new OauthRestController();
        new AppRestController();
    }


}
