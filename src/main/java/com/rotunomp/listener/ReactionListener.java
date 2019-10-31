package com.rotunomp.listener;

import com.rotunomp.services.SpotifyService;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {
    private String artist;
    private SpotifyService spotifyService;

    public ReactionListener() {
        spotifyService = SpotifyService.getService();
    }

    @Override
    public void onPrivateMessageReactionAdd (PrivateMessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        System.out.println("Received a private reaction event");

        Message message = (Message) event.getChannel().retrieveMessageById(
                 event.getChannel().getLatestMessageId());
        User user = event.getUser();

        String content = message.getContentDisplay();

        String split[] = content.split("\n");

        switch (event.getReaction().toString()) {
            case "U+0031 U+20E3":
                spotifyService.followArtist(split[1].split(" ")[1], user.getId());
                break;
            case "U+0032 U+20E3":
                spotifyService.followArtist(split[2].split(" ")[1], user.getId());
                break;
            case "U+0033 U+20E3":
                spotifyService.followArtist(split[3].split(" ")[1], user.getId());
                break;
            default:
                break;
        }

        System.out.println(message);
    }
}
