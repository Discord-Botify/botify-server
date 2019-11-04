package com.rotunomp.operations;

public enum FunctionName {
    PING (
            FunctionType.ALL,
            "!ping",
            "!ping .... The bot responds with a friendly message",
            "The bot responds with a friendly message. This is a demonstration of the " +
                    "bot's multithreading capabilities, because you can interact with the bot " +
                    "while it is waiting to send its next message"
    ),
    ADD_ROLE (
            FunctionType.SERVER,
            "!add-role",
            "!add-role <user> <role> .... Give a member a role by mentioning the user and role",
            "TODO"
    ),
    REMOVE_ROLE (
            FunctionType.SERVER,
            "!remove-role",
            "!remove-role <user> <role> .... Remove a role by @mentioning the user and role",
            "TODO"
    ),
    POKEMON (
            FunctionType.ALL,
            "!pokemon",
            "!pokemon <pokemon name> .... get info about a Pokemon",
            "TODO"
    ),
    SPOTIFY (
            FunctionType.PRIVATE,
            "!spotify",
            "!spotify <command> .... Various commands to get notified about artist releases (try !help spotify)",
            "TODO"
    ),
    HELP (
            FunctionType.ALL,
            "!help",
            "!help <command name> .... Get information about commands. Make sure not to include the `!` for the command you're searching!",
            "INFINITE LOOP EXCEPTION"
    );

    public final FunctionType functionType;
    public final String command;
    public final String shortHelp;
    public final String longHelp;

    FunctionName(FunctionType functionType, String command, String shortHelp, String longHelp) {
        this.functionType = functionType;
        this.command = command;
        this.shortHelp = shortHelp;
        this.longHelp = longHelp;
    }
}
