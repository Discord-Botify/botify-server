package com.rotunomp.threads;

import com.rotunomp.app.SessionFactoryInstance;
import com.rotunomp.models.FollowedArtist;
import com.rotunomp.models.SpotifyUser;
import com.rotunomp.services.SpotifyService;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Artist;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
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
                List<AlbumSimplified> albumList =
                        spotifyService.getArtistsAlbums(apiArtist.getId());
                // If the apiArtist's album list is longer than our saved album list
                // we need to send the notification!
                if(albumList.size() > dbArtist.getAlbumCount()) {
                    // Send the notification to all of the followers!
                    for (SpotifyUser user : dbArtist.getFollowers()) {
                        System.out.println("Follower of " + dbArtist.getName() + ": " + user.getId());
                        sendAlbumUpdateNotification(albumList, user.getId(), apiArtist.getName());
                    }
                    // Also update the albumCount in the database
                    dbArtist.setAlbumCount(albumList.size());
                    session.update(dbArtist);
                }
            }

            // Commit all our DB changes and we're done! Whew.
            tx.commit();
            System.out.println("Album notification cycle complete!");
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

    }

    private void sendAlbumUpdateNotification(List<AlbumSimplified> albums, String userId, String artistName) {
        // Figure out the most recent album
        AlbumSimplified mostRecentAlbum = albums.get(0);
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
