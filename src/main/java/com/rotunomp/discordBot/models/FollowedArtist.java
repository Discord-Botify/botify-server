package com.rotunomp.discordBot.models;

import com.fasterxml.jackson.annotation.*;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "FollowedArtist", catalog = "discord_bot", uniqueConstraints = {
        @UniqueConstraint(columnNames = "artist_id")})
public class FollowedArtist implements Comparable<FollowedArtist> {

    @Id
    @Column(name="artist_id")
    private String id;
    private String name;
    private int albumCount;
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(
            name = "FollowedArtist_SpotifyUser",
            catalog = "discord_bot",
            joinColumns = { @JoinColumn(name = "artist_id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id") }
    )
    private transient Set<SpotifyUser> followers = new HashSet<>();

    public FollowedArtist() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public void setAlbumCount(int albumCount) {
        this.albumCount = albumCount;
    }

    public Set<SpotifyUser> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<SpotifyUser> followers) {
        this.followers = followers;
    }

    // The result is positive if the first string is lexicographically greater
    // than the second string else the result would be negative
    @Override
    public int compareTo(@NotNull FollowedArtist artist) {
        return name.compareTo(artist.getName());
    }
}
