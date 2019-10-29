package com.rotunomp.models;

import javax.persistence.*;

@Entity
public class SpotifyUser {

    @Id
    private String id;
    @ManyToOne(cascade = CascadeType.ALL, targetEntity = FollowedArtist.class)
    @JoinColumn(name="artist_id")
    private FollowedArtist followedArtist;

    public SpotifyUser() {}

}
