package com.rotunomp.discordBot.threads;

import com.rotunomp.discordBot.app.DiscordPrivateMessenger;
import com.rotunomp.discordBot.app.JDAInstance;
import com.rotunomp.discordBot.app.SessionFactoryInstance;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.models.AppUser;
import com.rotunomp.discordBot.services.SpotifyService;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Artist;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.JDBCConnectionException;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AlbumNotificationThread extends Thread {

    private SpotifyService spotifyService;
    private SessionFactory sessionFactory;
    private int minutesPerUpdate;

    public AlbumNotificationThread(int minutesPerUpdate) {
        spotifyService = SpotifyService.getService();
        sessionFactory = SessionFactoryInstance.getInstance();
        this.minutesPerUpdate = minutesPerUpdate;
    }

    @Override
    public void run() {
        LocalDateTime timeSinceLastUpdate = LocalDateTime.now().minusMinutes(minutesPerUpdate);
        while (true) {
            LocalDateTime currentTime = LocalDateTime.now();
            if (timeSinceLastUpdate.isBefore(currentTime.minusMinutes(minutesPerUpdate))) {
                System.out.println("Beginning the album notification process");
                startNotifyProcess();
                timeSinceLastUpdate = currentTime;
            }
            // We might as well sleep the thread for a bit to reduce compute power
            try {
                // 100 seconds
                sleep(100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void startNotifyProcess() {
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        try {

            // Get artists from DB
            List<FollowedArtist> dbArtists =
                    session.createQuery("FROM FollowedArtist").list();

            // Create a map of DB Artist ID => FollowedArtist
            HashMap<String, FollowedArtist> dbArtistAlbumCountMap = new HashMap<>();
            for (FollowedArtist dbArtist : dbArtists) {
                dbArtistAlbumCountMap.put(dbArtist.getId(), dbArtist);
            }

            // Get corresponding artists from API
            List<Artist> apiArtists =
                    spotifyService.getArtistList(dbArtists);

            // Now that we have the artists from spotify and the
            // artistId => FollowedArtist map, we can do our logic
            // Loop over the apiArtists
            for (Artist apiArtist : apiArtists) {
                FollowedArtist dbArtist =
                        dbArtistAlbumCountMap.get(apiArtist.getId());
                List<Album> albumList =
                        spotifyService.getOrderedArtistReleases(apiArtist.getId());
                // If the apiArtist's album list is longer than our saved album list
                // we need to send the notification!
                if (albumList.size() > dbArtist.getAlbumCount()) {
                    // Send the notification to all of the followers!
                    for (AppUser user : dbArtist.getFollowers()) {
                        System.out.println("Follower of " + dbArtist.getName() + ": " + user.getDiscordId());
                        sendAlbumUpdateNotification(albumList, user.getDiscordId(), apiArtist.getName());
                    }
                    tx = session.beginTransaction();
                    // Also update the albumCount in the database
                    dbArtist.setAlbumCount(albumList.size());
                    session.update(dbArtist);
                    tx.commit();
                }
                // Let's sleep the thread for a bit. This will both speed up other
                // operations and reduce the hit on our rate limit
                Thread.sleep(500);
            }

            System.out.println("Album notification cycle complete!");
        } catch (JDBCConnectionException e) {
            // database connection dropped, it will reset automatically
            System.err.println("Database connection closed. Restarting the album notification process.");
            startNotifyProcess();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

    }

    private void sendAlbumUpdateNotification(List<Album> albums, String userId, String artistName) throws LoginException {
        // Figure out the most recent album
        Album mostRecentAlbum = albums.get(0);
        // Construct the message we want to send
        StringBuilder str = new StringBuilder();
        str.append("New ")
                .append(mostRecentAlbum.getAlbumType().type)
                .append(" from ")
                .append(artistName)
                .append(" : ")
                .append(mostRecentAlbum.getName())
                .append(" | https://open.spotify.com/album/")
                .append(mostRecentAlbum.getId());

        DiscordPrivateMessenger.sendMessage(str.toString(), userId);
    }

    private void sortAlbumsByReleaseDate(List<AlbumSimplified> albumList) {
        albumList.sort(new Comparator<AlbumSimplified>() {
            @Override
            public int compare(AlbumSimplified a1, AlbumSimplified a2) {
                // Turn the release dates of both albums into LocalDate objects
                LocalDateTime a1Release = LocalDateTime.parse(a1.getReleaseDate());
                LocalDateTime a2Release = LocalDateTime.parse(a2.getReleaseDate());

                if (a1Release.equals(a2Release))
                    return 0;
                return a1Release.isBefore(a2Release) ? -1 : 1;
            }
        });
    }
}
