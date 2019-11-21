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
    private DiscordService discordService;
    private AppSessionService appSessionService;

    public DiscordRestController() {
        discordService = DiscordService.getInstance();
        appSessionService = AppSessionService.getInstance();

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

            // Set status to 201
            response.status(201);

            // Return the session ID and the user info in JSON format
            returnJson.append("sessionId", appSessionId);
            System.out.println("ReturnJson: " + returnJson.toString());
            return returnJson;
        }, json());

    }

}
