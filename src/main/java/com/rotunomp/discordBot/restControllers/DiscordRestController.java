package com.rotunomp.discordBot.restControllers;

import static com.rotunomp.discordBot.app.JsonUtil.*;
import static spark.Spark.*;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.rotunomp.discordBot.services.AppSessionService;
import com.rotunomp.discordBot.services.DiscordService;

import static com.rotunomp.discordBot.app.JsonUtil.*;

/**
 * DiscordRestController
 */
public class DiscordRestController {
    String code;
    DiscordService discordService;
    AppSessionService appSessionService;

    public DiscordRestController() {
        discordService = DiscordService.getInstance();
        appSessionService = AppSessionService.getInstance();
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


        post("/oauth/discord", (request, response) -> {
            response.type("application/json");

            // Login the user through Discord OAuth and get their Discord user info
            // Also create them in the database if they don't exist
            String body = request.body();
            JSONObject jsonBody = new JSONObject(body);
            String code = jsonBody.getString("code");
            JSONObject returnJson = discordService.loginUser(code);

            // Start a session for the user
            String discordId = returnJson.getString("id");
            String appSessionId = appSessionService.startAppSession(discordId);

            // Return the session ID and the user info in JSON format
            returnJson.append("sessionId", appSessionId);
            return returnJson;
        }, jsonWithExposeAnnotation());


    }

}