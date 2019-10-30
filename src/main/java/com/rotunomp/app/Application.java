package com.rotunomp.app;

import com.rotunomp.listener.PrivateListener;
import com.rotunomp.listener.ServerListener;
import com.rotunomp.threads.AlbumNotificationThread;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class Application {
    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Properties.get("discord_token"));
        builder.addEventListeners(new ServerListener(), new PrivateListener());
        JDA jda = builder.build();

        // TODO: While loop that on certain times executes an update for notifications
        // We want to call this function when we run the bot, and every four hours after that
//        LocalDateTime timeSinceLastUpdate = LocalDateTime.now().minusHours(4);
//        while (true) {
//            LocalDateTime currentTime = LocalDateTime.now();
//            if(timeSinceLastUpdate.isBefore(currentTime.minusHours(4))) {
//                AlbumNotificationThread thread = new AlbumNotificationThread(jda);
//                thread.start();
//                timeSinceLastUpdate = currentTime;
//            }
//        }

    }

}
