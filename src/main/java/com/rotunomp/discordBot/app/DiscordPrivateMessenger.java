package com.rotunomp.discordBot.app;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.security.auth.login.LoginException;

public class DiscordPrivateMessenger {

    public static void sendMessage(String message, String discordId) throws LoginException {
        JDA jda = JDAInstance.getInstance();
        // Get the User
        User user = jda.getUserById(discordId);
        // Send the message
        if (user != null) {
            user.openPrivateChannel().queue((channel) ->
            {
                channel.sendMessage(message).queue();
            });
        }
    }

}
