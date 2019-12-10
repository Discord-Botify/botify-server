package com.rotunomp.discordBot.operations;

public enum FunctionName {
    PING (
            FunctionType.ALL,
            "!ping",
            "!ping .... The bot responds with a friendly message",
            "The bot responds with a friendly message. This is a demonstration of the " +
                    "bot's multithreading capabilities, because you can interact with the bot " +
                    "while it is waiting to send its next message"
    ),
    SPOTIFY (
            FunctionType.PRIVATE,
            "!spotify",
            "!spotify <command> .... Various commands to get notified about artist releases (try !help spotify)",
            "Usage: `!spotify <command>`\n\n" +
                    "**COMMANDS**\n" +
                    "`artist <artist name> .... Search for an artist based on their name`\n" +
                    "`follow <artist name> .... Follow an artist. The bot will respond with the first three search results for an artist, and you select which one to follow.`\n" +
                    "`follow-list .... List all the artists you are following`\n" +
                    "`unfollow .... The bot will reply with a list of everyone you follow, and lets you select which artists to unfollow.`"
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
