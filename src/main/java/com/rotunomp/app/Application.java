package com.rotunomp.app;

import com.rotunomp.listener.PrivateListener;
import com.rotunomp.listener.ServerListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Application {
    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Properties.get("discord_token"));
        builder.addEventListeners(new ServerListener(), new PrivateListener());
        builder.build();

        // TODO: While loop that on certain times executes an update for notifications

        System.out.println("Hi");
    }

}
