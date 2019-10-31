package com.rotunomp.listener;

import com.rotunomp.services.SpotifyService;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {
    String artist;
    SpotifyService spotifyService;

    public ReactionListener() {
        spotifyService = SpotifyService.getService();
    }

    public void onPrivateMessageReactionEvent (PrivateMessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

         String message = event.getChannel().retrieveMessageById(
                 event.getChannel().getLatestMessageId())
                 .toString();

        switch (event.getReaction().toString()) {
            case "U+0031 U+20E3":
                break;
            case "U+0032 U+20E3":
                break;
            case "U+0033 U+20E3":
                break;
            default:
                break;
        }
    }
}
