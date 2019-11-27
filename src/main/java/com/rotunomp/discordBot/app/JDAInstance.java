package com.rotunomp.discordBot.app;

import com.rotunomp.discordBot.listener.PrivateListener;
import com.rotunomp.discordBot.listener.ReactionListener;
import com.rotunomp.discordBot.listener.ServerListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class JDAInstance {

    private static JDAInstance jdaInstance;
    private static JDA jda;

    public static JDA getInstance() throws LoginException {
        if(jda == null) {
            jdaInstance = new JDAInstance();
        }
        return jda;
    }

    private JDAInstance() throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Properties.get("discord_token"));
        builder.addEventListeners(new ServerListener(), new PrivateListener(), new ReactionListener());
        jda = builder.build();

    }

}
