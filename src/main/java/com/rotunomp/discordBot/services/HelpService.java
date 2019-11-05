package com.rotunomp.discordBot.services;

import com.rotunomp.discordBot.operations.FunctionName;
import com.rotunomp.discordBot.operations.FunctionType;

public class HelpService {

    private static HelpService serviceInstance;

    public static HelpService getInstance() {
        if (serviceInstance == null) {
            serviceInstance = new HelpService();
        }
        return serviceInstance;
    }

    private HelpService() {
    }

    public String getDefaultHelpMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("This is the function help menu. All commands are listed below. To get more information on a specific command, do `!help <command>\n`");
        messageBuilder.append("Make sure not to include the ! in the command name you're searching for!\n");
        for (FunctionName e : FunctionName.class.getEnumConstants()) {
            messageBuilder.append("`").append(e.shortHelp).append("`\n");
        }

        return messageBuilder.toString();
    }

    public String getCommandHelpMessage(FunctionName functionName) {
        StringBuilder messageBuilder = new StringBuilder();

        // Describe where the command can be used
        messageBuilder.append("This command can be used ");
        if(functionName.functionType == FunctionType.SERVER) {
            messageBuilder.append("in a server only.\n");
        }
        else if (functionName.functionType == FunctionType.PRIVATE) {
            messageBuilder.append("in a private message only.\n");
        }
        else {
            messageBuilder.append("anywhere!\n");
        }

        messageBuilder.append(functionName.longHelp);

        return messageBuilder.toString();
    }
}
