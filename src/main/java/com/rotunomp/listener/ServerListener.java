package com.rotunomp.listener;

import com.rotunomp.apiWrappers.PokemonApiWrapper;
import com.rotunomp.operations.FunctionName;
import com.rotunomp.operations.FunctionType;
import com.rotunomp.services.PingService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;

public class ServerListener extends AbstractListener {

    private PingService pingService;

    public ServerListener() {
        super();
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
        this.channel = event.getChannel();

        // Initialize Member, Role, and Guild if we need them later
        Member member;
        Role role;
        Guild guild;

        if(this.functionNameHashMap.containsKey(command)) {
            System.out.println("Trying to execute command " + command + "...");
            FunctionName functionName = this.functionNameHashMap.get(command);
            if (functionName.functionType == FunctionType.PRIVATE) {
                channel.sendMessage("This is a private message-only command. Private message me this command instead of using it in a server!").queue();
                return;
            }
            switch (functionName) {
                case PING:
                    pingService = new PingService(this.channel, 5000);
                    pingService.start();
                    break;
                case REMOVE_ROLE:
                    member = message.getMentionedMembers().get(0);
                    role = message.getMentionedRoles().get(0);
                    guild = event.getGuild();
                    guild.removeRoleFromMember(member, role).queue();
                    this.channel.sendMessage(member.getUser().getAsMention() + " was removed from role " + role.getAsMention()).queue();
                    break;
                case ADD_ROLE:
                    member = message.getMentionedMembers().get(0);
                    role = message.getMentionedRoles().get(0);
                    guild = event.getGuild();
                    guild.addRoleToMember(member, role).queue();
                    this.channel.sendMessage(member.getUser().getAsMention() + " was added to role " + role.getAsMention()).queue();
                    break;
                case POKEMON:
                    String pokemonName = splitCommand[1];
                    PokemonApiWrapper pokemonApiWrapper = new PokemonApiWrapper();
                    this.channel.sendMessage(pokemonApiWrapper.getPokemonTypes(pokemonName)).queue();
                    this.channel.sendMessage(pokemonApiWrapper.getFlavorText(pokemonName)).queue();
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
                    this.channel.sendMessage("Command Error").queue();
                    break;
            }
        }

    }


}
