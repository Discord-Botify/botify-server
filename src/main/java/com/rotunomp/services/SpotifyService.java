package com.rotunomp.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.rotunomp.app.Properties;
import com.rotunomp.exceptions.ArtistNotFoundException;
import com.rotunomp.models.FollowedArtist;
import com.rotunomp.models.SpotifyUser;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.*;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class SpotifyService {

    private Api api;
    // This class is going to be a singleton
    private static SpotifyService serviceInstance;
    private SessionFactory sessionFactory;

    // Returns the singleton
    public static SpotifyService getService() {
        if (serviceInstance == null) {
            serviceInstance = new SpotifyService();
        }
        return serviceInstance;
    }

    private SpotifyService() {
        api = Api.builder()
                .clientId(Properties.get("spotify_client_id"))
                .clientSecret(Properties.get("spotify_client_secret"))
                .build();
        /* Create a request object. */
        final ClientCredentialsGrantRequest request = api.clientCredentialsGrant().build();

        /* Use the request object to make the request, either asynchronously (getAsync) or synchronously (get) */
        final SettableFuture<ClientCredentials> responseFuture = request.getAsync();

        /* Add callbacks to handle success and failure */
        Futures.addCallback(responseFuture, new FutureCallback<ClientCredentials>() {
            @Override
            public void onSuccess(ClientCredentials clientCredentials) {
                /* The tokens were retrieved successfully! */
                System.out.println("Successfully retrieved an access token! " + clientCredentials.getAccessToken());
                System.out.println("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");

                /* Set access token on the Api object so that it's used going forward */
                api.setAccessToken(clientCredentials.getAccessToken());

                /* Please note that this flow does not return a refresh token. * That's only for the Authorization code flow */
            }

            @Override
            public void onFailure(Throwable throwable) {
                /* An error occurred while getting the access token. This is probably caused by the client id or * client secret is invalid. */
            }
        });

        // Set up the Hibernate environment
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(FollowedArtist.class);
        configuration.addAnnotatedClass(SpotifyUser.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    public String getAlbumNameById(String albumId) {
        AlbumRequest request = api.getAlbum(albumId).build();
        try {
            Album album = request.get();
            return album.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Cannot find album: " + albumId;
    }

    private List<Artist> searchArtistsByName(String artistName, int listSize) throws ArtistNotFoundException, IOException {
        ArtistSearchRequest request = api.searchArtists(artistName).limit(listSize).build();
        try {
            return request.get().getItems();
        } catch (WebApiException e) {
            throw new ArtistNotFoundException();
        }
    }

    public String getArtistsStringByName(String artistName) {
        try {
            List<Artist> artists = searchArtistsByName(artistName, 3);
            if (artists.size() == 0) {
                return "No results for " + artistName;
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Search Results for ").append(artistName).append(": \n");
            for (Artist artist : artists) {
                stringBuilder.append(artist.getName())
                        .append(" | ID: ")
                        .append(artist.getId())
                        .append("\n");
            }
            return stringBuilder.toString();

        } catch (ArtistNotFoundException e) {
            e.printStackTrace();
            return "Could not find the specified artist";
        } catch (IOException e) {
            e.printStackTrace();
            return "Something went wrong. Try again later!";
        }
    }

    public void notifyUserAlbumUpdates() {
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
            ArtistsRequest artistsRequest =
                    api.getArtists(artistIds.toString()).build();
            List<Artist> apiArtists = artistsRequest.get();

            // Now that we have the artists from spotify and the
            // artistId => FollowedArtist map, we can do our logic
            for (Artist apiArtist : apiArtists) {

                FollowedArtist dbArtist =
                        dbArtistAlbumCountMap.get(apiArtist.getId());
                List<SimpleAlbum> albumList = getArtistsAlbums(apiArtist.getId());
            }

            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Get list of albums and singles for a given artist
    public List<SimpleAlbum> getArtistsAlbums(String artistId) {
        Session session = sessionFactory.openSession();

        try {
            AlbumsForArtistRequest request =
                    api.getAlbumsForArtist(artistId)
                            .market("US")
                            .types(AlbumType.ALBUM, AlbumType.SINGLE)
                            .build();
            List<SimpleAlbum> albums = request.get().getItems();
            return albums;
        } catch (WebApiException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String followArtist(String artistName) {
        try {
            // Grab a list of artists that the user might be intending to follow
            // It's possible that the user intended to follow a different artist
            // Than the Spotify API thinks, so we want to account for that
            List<Artist> potentialArtists = searchArtistsByName(artistName, 3);

            //

        } catch (ArtistNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
