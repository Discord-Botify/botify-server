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

        PrivateChannel channel = event.getChannel();

        Message message = channel.retrieveMessageById(
                 event.getReaction().getMessageId()).complete();
        User user = event.getUser();

        String content = message.getContentRaw();

        String[] split = content.split("\\r?\\n");

        System.out.println(event.getReaction().getReactionEmote().toString());

        String artistName;

        switch (event.getReaction().getReactionEmote().toString()) {
            case "RE:U+31U+20e3":
                artistName = split[1].split(" \\| ")[0];
                spotifyService.followArtist(artistName, user.getId());
                channel.sendMessage("Success following " + artistName).queue();
                break;
            case "RE:U+32U+20e3":
                artistName = split[2].split(" \\| ")[0];
                spotifyService.followArtist(artistName, user.getId());
                channel.sendMessage("Success following " + artistName).queue();
                break;
            case "RE:U+33U+20e3":
                artistName = split[3].split(" \\| ")[0];
                spotifyService.followArtist(artistName, user.getId());
                channel.sendMessage("Success following " + artistName).queue();
                break;
            default:
                break;
        }

    }
}
