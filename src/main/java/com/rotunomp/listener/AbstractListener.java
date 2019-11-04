package com.rotunomp.listener;

import com.rotunomp.operations.FunctionName;
import com.rotunomp.services.HelpService;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class AbstractListener extends ListenerAdapter {

    protected HashMap<String, FunctionName> functionNameHashMap;
    protected MessageChannel channel;
    protected HelpService helpService;

    protected AbstractListener() {
        functionNameHashMap = new HashMap<>();
        // Iterate through all the enums and put them into our hashmap
        // HashMap is String command => FunctionName
        for (FunctionName e : FunctionName.class.getEnumConstants()) {
            functionNameHashMap.put(e.command, e);
        }

        // Instantiate all services\
        helpService = HelpService.getInstance();
    }

    protected void sendLongMessage(MessageChannel channel, @NotNull String message) {
        for (int i = 0; i < message.length(); i+=2000) {
            if (i + 2000 > message.length()) channel.sendMessage(message.substring(i)).queue();
            else channel.sendMessage(message.substring(i,i+2000)).queue();
        }
    }

    protected void sendHelpMessage() {
        channel.sendMessage(helpService.getDefaultHelpMessage()).queue();
    }

    protected void sendHelpMessage(String command) {
        FunctionName functionName = functionNameHashMap.get("!" + command);
        System.out.println(functionName.longHelp);
        channel.sendMessage(helpService.getCommandHelpMessage(functionName)).queue();
    }


}
