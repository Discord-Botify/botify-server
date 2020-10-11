package com.rotunomp.discordBot.restControllers;

import com.rotunomp.discordBot.app.Properties;
import spark.Filter;
import spark.Spark;

import static spark.Spark.*;

public class RestController {

    public RestController() {
        // Set up SSL
//        String keyStoreLocation = Properties.get("ssl_certificate_location");
//        String keyStorePassword = Properties.get("ssl_password");
//        secure(keyStoreLocation, keyStorePassword, null, null);

        // Enable CORS for all endpoints
//        Spark.staticFiles.location("/assets");
//        Spark.staticFiles.header("Access-Control-Allow-Origin", "*");
//	    Spark.staticFiles.header("Access-Control-Allow-Methods", "*");

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
            System.out.println("We're in the before block for Spark");
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });

//        after((Filter) (request, response) -> {
//            response.header("Access-Control-Allow-Origin", "*");
//            response.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
//        });

        new SpotifyRestController();
        new OauthRestController();
        new AppRestController();
    }


}
