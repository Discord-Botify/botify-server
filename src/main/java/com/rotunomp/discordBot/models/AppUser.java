package com.rotunomp.discordBot.models;

import com.google.gson.annotations.Expose;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "SpotifyUser", catalog = "discord_bot", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")})
public class AppUser {

    @Id
    @Column(name = "user_id")
    @Expose
    private String discordId;
    @Expose
    private String spotifyId;
    @Expose
    private String spotifyUserName;
    @Expose
    private String spotifyRefreshToken;
    @ManyToMany(mappedBy = "followers", fetch = FetchType.EAGER)
    @Expose
    private Set<FollowedArtist> followedArtists = new HashSet<>();

    public AppUser() {}

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public Set<FollowedArtist> getFollowedArtists() {
        return followedArtists;
    }

    public void setFollowedArtists(Set<FollowedArtist> followedArtists) {
        this.followedArtists = followedArtists;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getSpotifyUserName() {
        return spotifyUserName;
    }

    public void setSpotifyUserName(String spotifyUserName) {
        this.spotifyUserName = spotifyUserName;
    }

    public String getSpotifyRefreshToken() {
        return spotifyRefreshToken;
    }

    public void setSpotifyRefreshToken(String spotifyRefreshToken) {
        this.spotifyRefreshToken = spotifyRefreshToken;
    }
}
