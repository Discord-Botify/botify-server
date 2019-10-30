package com.rotunomp.models;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "FollowedArtist", catalog = "discord_bot", uniqueConstraints = {
        @UniqueConstraint(columnNames = "artist_id")})
public class FollowedArtist {

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
    private Set<SpotifyUser> followers = new HashSet<>();

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
}
