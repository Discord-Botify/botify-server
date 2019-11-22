package com.rotunomp.discordBot.listener;

import com.rotunomp.discordBot.services.SpotifyService;
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

        PrivateChannel channel = event.getChannel();

        Message message = channel.retrieveMessageById(
                 event.getReaction().getMessageId()).complete();
        User user = event.getUser();

        String content = message.getContentRaw();

        String[] split = content.split("\\r?\\n");

        //System.out.println(event.getReaction().getReactionEmote().getEmoji());

        String artistName;
        String artistId;

        switch (event.getReaction().getReactionEmote().toString()) {
            //Follows artist
            case "RE:U+31U+20e3":
                artistId = split[1].split(" \\| ID: ")[1];
                spotifyService.followArtistById(artistId, user.getId());
                channel.sendMessage("Success following " + artistName).queue();
                break;
            case "RE:U+32U+20e3":
                artistId = split[2].split(" \\| ID: ")[1];
                spotifyService.followArtistById(artistId, user.getId());
                channel.sendMessage("Success following " + artistName).queue();
                break;
            case "RE:U+33U+20e3":
                artistId = split[3].split(" \\| ID: ")[1];
                spotifyService.followArtistById(artistId, user.getId());
                channel.sendMessage("Success following " + artistName).queue();
                break;

            // Unfollows artist
            case "RE:U+1f1e6":
                artistName = split[0].split(" \\| ")[0].substring(23);
                artistId = split[0].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1e7":
                artistName = split[1].split(" \\| ")[0].substring(23);
                artistId = split[1].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1e8":
                artistName = split[2].split(" \\| ")[0].substring(23);
                artistId = split[2].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1e9":
                artistName = split[3].split(" \\| ")[0].substring(23);
                artistId = split[3].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1ea":
                artistName = split[4].split(" \\| ")[0].substring(23);
                artistId = split[4].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1eb":
                artistName = split[5].split(" \\| ")[0].substring(23);
                artistId = split[5].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1ec":
                artistName = split[6].split(" \\| ")[0].substring(23);
                artistId = split[6].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1ed":
                artistName = split[7].split(" \\| ")[0].substring(23);
                artistId = split[7].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1ee":
                artistName = split[8].split(" \\| ")[0].substring(23);
                artistId = split[8].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            case "RE:U+1f1ef":
                artistName = split[9].split(" \\| ")[0].substring(23);
                artistId = split[9].split(" \\| ID: ")[1];
                System.out.println(artistName);
                spotifyService.unfollowArtist(artistId, user.getId());
                channel.sendMessage("Success unfollowing " + artistName).queue();
                break;
            default:
                break;
        }

    }
}
