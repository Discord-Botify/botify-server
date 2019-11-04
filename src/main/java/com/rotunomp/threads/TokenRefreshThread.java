package com.rotunomp.threads;

import com.rotunomp.services.SpotifyService;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.time.LocalDateTime;

public class TokenRefreshThread extends Thread{

    private SpotifyApi spotifyApi;

    public TokenRefreshThread(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public void run() {
        // The Spotify token expires every hour, 50 minutes is good
        final int minutesPerSpotifyRefresh = 50;
        LocalDateTime timeSinceLastUpdate = LocalDateTime.now();
        while (true) {
            LocalDateTime currentTime = LocalDateTime.now();
            if(timeSinceLastUpdate.isBefore(currentTime.minusMinutes(minutesPerSpotifyRefresh))) {
                System.out.println("Beginning the token refresh process");
                refreshCredentials();
                timeSinceLastUpdate = currentTime;
            }
            // We might as well sleep the thread for a bit to reduce compute power
            try {
                // 300 seconds = 5 minutes
                sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void refreshCredentials() {
        final ClientCredentialsRequest request = spotifyApi.clientCredentials().build();

        /* Use the request object to make the request, either asynchronously (getAsync) or synchronously (get) */
        final ClientCredentials clientCredentials;
        try {
            clientCredentials = request.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            System.out.println("Successfully retrieved an access token! " + clientCredentials.getAccessToken());
            System.out.println("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");
        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
            System.out.println("Error connecting to Spotify");
        }

    }

}
