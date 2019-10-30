package com.rotunomp.listener;

import com.rotunomp.operations.FunctionName;
import com.rotunomp.services.SpotifyService;
import com.wrapper.spotify.models.SimpleAlbum;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;

public class PrivateListener extends ListenerAdapter {

    private HashMap<String, FunctionName> functionNameHashMap;
    private SpotifyService spotifyService;

    public PrivateListener() {
        functionNameHashMap = new HashMap<>();
        // Iterate through all the enums and put them into our hashmap
        // HashMap is String command => FunctionName
        for (FunctionName e : FunctionName.class.getEnumConstants()) {
            functionNameHashMap.put(e.command, e);
        }

        // Instantiate all the services
        spotifyService = SpotifyService.getService();
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        System.out.println("Received a message from " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay());

        // Parse out the command and put the command parameters in a String array
        // Which we can parse per command
        Message message = event.getMessage();
        String messageRaw = message.getContentRaw();
        String[] splitCommand = messageRaw.split(" ");
        String command = splitCommand[0];
        MessageChannel channel = event.getChannel();

        // Initialize Member, Role, and Guild if we need them later
        Member member;
        Role role;
        Guild guild;

        if (functionNameHashMap.containsKey(command)) {
            System.out.println("Trying to execute command " + command + "...");
            switch (functionNameHashMap.get(command)) {  // Gets the FunctionName Enum
                case SPOTIFY:
                    switch (splitCommand[1]) {
                        case "album":
                            channel.sendMessage(
                                    spotifyService.getAlbumNameById(splitCommand[2])
                            ).queue();
                            break;
                        case "artist":
                            StringBuilder str = new StringBuilder();
                            // Loop over all remaining text in the command to get full artist name
                            for (int i = 2; i < splitCommand.length; i++) {
                                str.append(splitCommand[i]).append(" ");
                            }
                            channel.sendMessage(
                                    spotifyService.getArtistsStringByName(str.toString()))
                                    .queue();
                            channel.addReactionById(channel.getLatestMessageId(),"U+0031 U+20E3");
                            channel.addReactionById(channel.getLatestMessageId(),"U+0032 U+20E3");
                            channel.addReactionById(channel.getLatestMessageId(),"U+0033 U+20E3");
                            break;
                        case "albums":
                            List<SimpleAlbum> albums = spotifyService.getArtistsAlbums(splitCommand[2]);
                            for (SimpleAlbum album : albums) {
                                System.out.println(album.toString());
                            }
                            break;
                        case "follow":
                            StringBuilder artist = new StringBuilder();
                            // Loop over all remaining text in the command to get full artist name
                            for (int i = 2; i < splitCommand.length; i++) {
                                artist.append(splitCommand[i]).append(" ");
                            }
                            User user = message.getAuthor();
                            channel.sendMessage(
                                    spotifyService.followArtist(artist.toString(),
                                            user.getId())
                            ).queue();
                            break;
                        default:
                            channel.sendMessage("Usage: !spotify album/artist <id/name>").queue();
                            break;
                    }
            }
        }

    }


}
