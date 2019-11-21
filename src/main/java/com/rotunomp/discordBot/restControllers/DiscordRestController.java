package com.rotunomp.discordBot.restControllers;

import static com.rotunomp.discordBot.app.JsonUtil.*;
import static spark.Spark.*;
import com.rotunomp.discordBot.app.Properties;

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
import java.awt.*;

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

        before("/*", (request, response) -> {
	    response.header("Access-Control-Allow-Origin", "*");
	    response.header("Access-Control-Allow-Headers", "*");
	    response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
	    response.type("application/json");
	});

        post("/oauth/discord", (request, response) -> {

            // Login the user through Discord OAuth and get their Discord user info
            // Also create them in the database if they don't exist
            String body = request.body();
            JSONObject jsonBody = new JSONObject(body);
            String code = jsonBody.getString("code");
            JSONObject returnJson = discordService.loginUser(code);

            // Start a session for the user
            String discordId = returnJson.getString("id");
            String appSessionId = appSessionService.startAppSession(discordId);

	    // Put redirect in the response
	    response.redirect("/home");
	    response.status(201);

            // Return the session ID and the user info in JSON format
            returnJson.append("sessionId", appSessionId);
	    System.out.println("ReturnJson: " + returnJson.toString());
            return "";
        }, json());

	post("/test", "application/json", (request, response) -> {
    	    response.status(200);
	    System.out.println("In the POST test!");

	    return new Color(1);
	}, json());

	get("/testget", (request, response) -> "Hello!", json());
    }

}
