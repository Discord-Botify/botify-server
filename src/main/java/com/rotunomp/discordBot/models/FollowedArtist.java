package com.rotunomp.discordBot.models;

import com.google.gson.annotations.Expose;
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
    @Expose
    private String id;
    @Expose
    private String name;
    @Expose
    private int albumCount;
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(
            name = "FollowedArtist_SpotifyUser",
            catalog = "discord_bot",
            joinColumns = { @JoinColumn(name = "artist_id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id") }
    )
    private Set<AppUser> followers = new HashSet<>();

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

    public Set<AppUser> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<AppUser> followers) {
        this.followers = followers;
    }

    // The result is positive if the first string is lexicographically greater
    // than the second string else the result would be negative
    @Override
    public int compareTo(@NotNull FollowedArtist artist) {
        return name.compareTo(artist.getName());
    }
}
