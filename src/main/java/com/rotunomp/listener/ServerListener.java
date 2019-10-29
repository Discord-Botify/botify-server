package com.rotunomp.listener;

import com.rotunomp.apiWrappers.PokemonApiWrapper;
import com.rotunomp.operations.FunctionName;
import com.rotunomp.services.PingService;
import com.rotunomp.services.SpotifyService;
import com.wrapper.spotify.models.SimpleAlbum;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;

public class ServerListener extends ListenerAdapter {

    private HashMap<String, FunctionName> functionNameHashMap;
    private PingService pingService;

    public ServerListener() {
        functionNameHashMap = new HashMap<>();
        // Iterate through all the enums and put them into our hashmap
        // HashMap is String command => FunctionName
        for (FunctionName e : FunctionName.class.getEnumConstants()) {
            functionNameHashMap.put(e.command, e);
        };

        // Instantiate all the services
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
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

        if(functionNameHashMap.containsKey(command)) {
            System.out.println("Trying to execute command " + command + "...");
            switch (functionNameHashMap.get(command)) {  // Gets the FunctionName Enum
                case PING:
                    pingService = new PingService(channel, 2000);
//                    pingService.pong();
                    pingService.start();
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
                default:
                    channel.sendMessage("Command Error").queue();
                    break;

            }
        }

    }

}
