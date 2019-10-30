package com.rotunomp.threads;

import com.rotunomp.app.SessionFactoryInstance;
import com.rotunomp.models.FollowedArtist;
import com.rotunomp.models.SpotifyUser;
import com.rotunomp.services.SpotifyService;
import com.wrapper.spotify.methods.ArtistsRequest;
import com.wrapper.spotify.methods.UserRequest;
import com.wrapper.spotify.models.Album;
import com.wrapper.spotify.models.Artist;
import com.wrapper.spotify.models.SimpleAlbum;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.entities.UserImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.List;

public class AlbumNotificationThread extends Thread {

    private SpotifyService spotifyService;
    private SessionFactory sessionFactory;
    private JDA jda;

    public AlbumNotificationThread(JDA jda) {
        spotifyService = SpotifyService.getService();
        sessionFactory = SessionFactoryInstance.getInstance();
        this.jda = jda;
    }

    @Override
    public void run() {
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            // Get artists from DB
            List<FollowedArtist> dbArtists =
                    session.createQuery("FROM FollowedArtist").list();

            // Create a map of DB Artist ID => FollowedArtist
            HashMap<String, FollowedArtist> dbArtistAlbumCountMap = new HashMap<>();

            // Get corresponding artists from API
            // First we have to build a string of the artist ids
            StringBuilder artistIds = new StringBuilder();
            for (FollowedArtist followedArtist : dbArtists) {
                artistIds.append(followedArtist.getId()).append(",");
                // Also stick the artist ID and album count in the map
                dbArtistAlbumCountMap.put
                        (followedArtist.getId(), followedArtist);
            }
            // Remove trailing comma
            artistIds.deleteCharAt(artistIds.length() - 1);
            List<Artist> apiArtists =
                    spotifyService.getArtistList(artistIds.toString());

            // Now that we have the artists from spotify and the
            // artistId => FollowedArtist map, we can do our logic
            // Loop over the apiArtists
            for (Artist apiArtist : apiArtists) {
                FollowedArtist dbArtist =
                        dbArtistAlbumCountMap.get(apiArtist.getId());
                List<SimpleAlbum> albumList =
                        spotifyService.getArtistsAlbums(apiArtist.getId());
                // If the apiArtist's album list is longer than our saved album list
                // we need to send the notification!
                if(albumList.size() > dbArtist.getAlbumCount()) {
                    // Send the notification to all of the followers!
                    for (SpotifyUser user : dbArtist.getFollowers()) {
                        sendAlbumUpdateNotification(albumList, user.getId(), apiArtist.getName());
                    }
                    // Also update the albumCount in the database
                    dbArtist.setAlbumCount(albumList.size());
                    session.update(dbArtist);
                }
            }

            // Commit all our DB changes and we're done! Whew.
            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

    }

    private void sendAlbumUpdateNotification(List<SimpleAlbum> albums, String userId, String artistName) {
        // Figure out the most recent album
        SimpleAlbum mostRecentAlbum = albums.get(0);
        // Construct the message we want to send
        StringBuilder str = new StringBuilder();
        str.append("New")
        .append(mostRecentAlbum.getAlbumType().type)
        .append("from")
        .append(artistName)
        .append(":")
        .append(mostRecentAlbum.getName())
        .append("|")
        .append(mostRecentAlbum.getHref());

        sendMessageToUser(str.toString(), userId);
    }

    private void sendMessageToUser(String message, String userId) {
        // Get the User
        User user = jda.getUserById(userId);
        // Send the message
        user.openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(message).queue();
        });
    }
}
