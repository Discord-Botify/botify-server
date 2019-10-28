package com.rotunomp.listener;

import com.rotunomp.apiWrappers.PokemonApiWrapper;
import com.rotunomp.operations.FunctionName;
import com.rotunomp.services.SpotifyService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;

public class CommandListener extends ListenerAdapter {

    private HashMap<String, FunctionName> functionNameHashMap;
    private SpotifyService spotifyService;

    public CommandListener() {
        functionNameHashMap = new HashMap<>();
        // Iterate through all the enums and put them into our hashmap
        // HashMap is String command => FunctionName
        for (FunctionName e : FunctionName.class.getEnumConstants()) {
            functionNameHashMap.put(e.command, e);
        };

        // Instantiate all the services
        spotifyService = SpotifyService.getService();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Right away ignore all messages from bots
        if(event.getAuthor().isBot()) {
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

        System.out.println("Trying to execute command " + command + "...");
        // !add-role user role

        if(functionNameHashMap.containsKey(command)) {
            switch (functionNameHashMap.get(command)) {  // Gets the FunctionName Enum
                case PING:
                    channel.sendMessage("Pong!").queue();
                    break;
                case REMOVE_ROLE:
                    member = message.getMentionedMembers().get(0);
                    role = message.getMentionedRoles().get(0);
                    guild = event.getGuild();
                    guild.removeRoleFromMember(member, role).queue();
                    channel.sendMessage(member.getUser().getAsMention() + " was removed from role " + role.getAsMention()).queue();
                    break;
                case ADD_ROLE:
                    member = message.getMentionedMembers().get(0);
                    role = message.getMentionedRoles().get(0);
                    guild = event.getGuild();
                    guild.addRoleToMember(member, role).queue();
                    channel.sendMessage(member.getUser().getAsMention() + " was added to role " + role.getAsMention()).queue();
                    break;
                case POKEMON:
                    String pokemonName = splitCommand[1];
                    PokemonApiWrapper pokemonApiWrapper = new PokemonApiWrapper();
                    channel.sendMessage(pokemonApiWrapper.getPokemonTypes(pokemonName)).queue();
                    channel.sendMessage(pokemonApiWrapper.getFlavorText(pokemonName)).queue();
                    break;
                case SPOTIFY:
                    switch (splitCommand[1]) {
                        case "album":
                            channel.sendMessage(spotifyService.getAlbumName(splitCommand[2])).queue();
                            break;
                        case "artist":
                            StringBuilder str = new StringBuilder();
                            // Loop over all remaining text in the command
                            for (int i = 2; i < splitCommand.length; i++) {
                                str.append(splitCommand[i]).append(" ");
                            }
                            channel.sendMessage(
                                    spotifyService.getArtistsStringByName(str.toString())).queue();
                            break;
                        default:
                            channel.sendMessage("Usage: !spotify album/artist <id/name>").queue();
                    }
                    break;
                default:
                    channel.sendMessage("Command Error").queue();
                    break;

            }
        }

    }

}
