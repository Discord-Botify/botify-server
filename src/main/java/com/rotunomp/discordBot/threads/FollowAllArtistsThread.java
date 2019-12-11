package com.rotunomp.discordBot.threads;

import com.rotunomp.discordBot.services.SpotifyService;

public class FollowAllArtistsThread extends Thread {

    private SpotifyService spotifyService;

    public FollowAllArtistsThread() {
        this.spotifyService = SpotifyService.getService();
    }

    public void run() {

    }

}
