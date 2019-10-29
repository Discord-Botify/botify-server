package com.rotunomp.models;

import javax.persistence.*;
import java.util.List;

@Entity
public class FollowedArtist {

    @Id
    private String id;
    private String name;
    private int albumCount;
    @OneToMany(cascade=CascadeType.ALL, targetEntity = SpotifyUser.class)
    private List<SpotifyUser> followers;

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

    public List<SpotifyUser> getFollowers() {
        return followers;
    }

    public void setFollowers(List<SpotifyUser> followers) {
        this.followers = followers;
    }
}
