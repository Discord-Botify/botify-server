package com.rotunomp.app;

import com.rotunomp.listener.PrivateListener;
import com.rotunomp.listener.ReactionListener;
import com.rotunomp.listener.ServerListener;
import com.rotunomp.threads.AlbumNotificationThread;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

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
    }

}
