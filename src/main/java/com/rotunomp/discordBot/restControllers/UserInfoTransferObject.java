package com.rotunomp.discordBot.restControllers;

public class UserInfoTransferObject {
    private String appSessionId;
    private String discordName;
    private String discordDiscriminator;
    private String spotifyUserName;

    public UserInfoTransferObject() {

    }

    public String getAppSessionId() {
        return appSessionId;
    }

    public void setAppSessionId(String appSessionId) {
        this.appSessionId = appSessionId;
    }

    public String getDiscordName() {
        return discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public String getDiscordDiscriminator() {
        return discordDiscriminator;
    }

    public void setDiscordDiscriminator(String discordDiscriminator) {
        this.discordDiscriminator = discordDiscriminator;
    }

    public String getSpotifyUserName() {
        return spotifyUserName;
    }

    public void setSpotifyUserName(String spotifyUserName) {
        this.spotifyUserName = spotifyUserName;
    }
}
