package com.rotunomp.discordBot.app;

import com.rotunomp.discordBot.listener.PrivateListener;
import com.rotunomp.discordBot.listener.ReactionListener;
import com.rotunomp.discordBot.listener.ServerListener;
import com.rotunomp.discordBot.restControllers.RestController;
import com.rotunomp.discordBot.threads.AlbumNotificationThread;
import com.rotunomp.discordBot.threads.DatabaseCleanupThread;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;


import javax.security.auth.login.LoginException;

public class Application {

    public static void main(String[] args) throws LoginException {
        // Build the Discord Bot
        JDA jda = JDAInstance.getInstance();

        // This begins a thread that controls the user notification
        // The thread will repeat the notification process every x minutes
        int minutesPerUpdate = 300; // x
        AlbumNotificationThread thread = new AlbumNotificationThread(minutesPerUpdate);
        thread.start();

        // Begins the thread which will daily wipe out the expired sessionIds
        DatabaseCleanupThread cleanupThread = new DatabaseCleanupThread();
        cleanupThread.start();

        // Start the REST API
        new RestController();
    }

}
