package com.rotunomp.listener;

import com.rotunomp.operations.FunctionName;
import com.rotunomp.operations.FunctionType;
import com.rotunomp.services.SpotifyService;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.HashMap;
import java.util.List;

public class PrivateListener extends AbstractListener {

    private SpotifyService spotifyService;

    public PrivateListener() {
        super();
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
        this.channel = event.getChannel();

        // Initialize Member, Role, and Guild if we need them later
        Member member;
        Role role;
        Guild guild;

        if (this.functionNameHashMap.containsKey(command)) {
            System.out.println("Trying to execute command " + command + "...");
            FunctionName functionName = this.functionNameHashMap.get(command);
            if (functionName.functionType == FunctionType.SERVER) {
                channel.sendMessage("This is a server-only command. Use this command in a server, not a private message").queue();
                return;
            }
            switch (functionName) {  // Gets the FunctionName Enum
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
                                    .queue(message1 -> {
                                        message1.addReaction("U+0031 U+20E3").queue();
                                        message1.addReaction("U+0032 U+20E3").queue();
                                        message1.addReaction("U+0033 U+20E3").queue();
                                    });
                            break;
                        case "albums":
                            List<AlbumSimplified> albums = spotifyService.getArtistsAlbums(splitCommand[2]);
                            for (AlbumSimplified album : albums) {
                                System.out.println(album.toString());
                            }
                            break;
                        case "follow":
                            str = new StringBuilder();
                            // Loop over all remaining text in the command to get full artist name
                            for (int i = 2; i < splitCommand.length; i++) {
                                str.append(splitCommand[i]).append(" ");
                            }
                            channel.sendMessage(
                                    spotifyService.getArtistsStringByName(str.toString()))
                                    .queue(message1 -> {
                                        message1.addReaction("U+0031 U+20E3").queue();
                                        message1.addReaction("U+0032 U+20E3").queue();
                                        message1.addReaction("U+0033 U+20E3").queue();
                                    });

                            break;
                        case "unfollow":
                            String artistId = splitCommand[2];
                            channel.sendMessage(artistId).queue();
                            channel.sendMessage(event.getAuthor().getId()).queue();
                            channel.sendMessage(spotifyService.getFollowedArtistStringForUser(
                                    event.getAuthor().getId())).queue(unfollowMessage -> {
                                        unfollowMessage.addReaction("U+1F1E6").queue();;
                            });

                            channel.sendMessage(
                                    spotifyService.unfollowArtist(artistId, event.getAuthor().getId())
                            ).queue();
                            break;
                        case "follow-list":
                            channel.sendMessage(
                                    spotifyService.getFollowedArtistStringForUser(
                                            event.getAuthor().getId()
                                    )
                            ).queue();
                            break;
                        default:
                            channel.sendMessage("Usage: !spotify album/artist <id/name>").queue();
                            break;
                    }
                    break;
                case HELP:
                    // See if the command has a second parameter and call the help function
                    // accordingly
                    try {
                        this.sendHelpMessage(splitCommand[1]);
                    } catch (Exception e) {
                        this.sendHelpMessage();
                    }
                    break;
                default:
                    break;
            }
        }

    }


}
