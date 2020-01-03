package com.rotunomp.discordBot.listener;

import com.rotunomp.discordBot.operations.FunctionName;
import com.rotunomp.discordBot.operations.FunctionType;
import com.rotunomp.discordBot.services.PingService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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

    // When a member joins, send them a welcome message
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User newMember = event.getMember().getUser();
        newMember.openPrivateChannel().queue((channel) -> {
            channel.sendMessage("Welcome to Botify! Type !help to see Discord commands").queue();
        });

    }


}
