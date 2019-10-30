package com.rotunomp.models;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "SpotifyUser", catalog = "discord_bot", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")})
public class SpotifyUser {

    @Id
    @Column(name = "user_id")
    private String id;
    @ManyToMany(mappedBy = "followers")
    private Set<FollowedArtist> followedArtists = new HashSet<>();

    public SpotifyUser() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<FollowedArtist> getFollowedArtists() {
        return followedArtists;
    }

    public void setFollowedArtists(Set<FollowedArtist> followedArtists) {
        this.followedArtists = followedArtists;
    }
}
