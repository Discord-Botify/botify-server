package com.rotunomp.app;

import com.rotunomp.listener.CommandListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Application {
    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken("NjM2OTg1MjMwODgzODE1NDU0.XbHlQw.fEIYq2_--hUzwskEi5AQq6o3iNo");
        builder.addEventListeners(new CommandListener());
        builder.build();

        // TODO: While loop that on certain times executes an update for notifications

        System.out.println("Hi");
    }

}
