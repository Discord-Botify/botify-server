package com.rotunomp.discordBot.app;

import com.google.gson.Gson;
import com.rotunomp.discordBot.listener.PrivateListener;
import com.rotunomp.discordBot.listener.ReactionListener;
import com.rotunomp.discordBot.listener.ServerListener;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.restControllers.DiscordRestController;
import com.rotunomp.discordBot.restControllers.RestController;
import com.rotunomp.discordBot.restControllers.SpotifyRestController;
import com.rotunomp.discordBot.restControllers.DiscordRestController;
import com.rotunomp.discordBot.services.SpotifyService;
import com.rotunomp.discordBot.threads.AlbumNotificationThread;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;


import javax.security.auth.login.LoginException;
import java.util.List;

public class Application {

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Properties.get("discord_token"));
        builder.addEventListeners(new ServerListener(), new PrivateListener(), new ReactionListener());
        JDA jda = builder.build();

        // This begins a thread that controls the user notification
        // The thread will repeat the notification process every x minutes
        int minutesPerUpdate = 60; // x
        AlbumNotificationThread thread = new AlbumNotificationThread(jda, minutesPerUpdate);
        thread.start();

        // Start the REST API
        new RestController();
    }

}
