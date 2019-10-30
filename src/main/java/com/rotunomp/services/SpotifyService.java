package com.rotunomp.services;

import com.google.common.collect.ForwardingQueue;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.rotunomp.app.Properties;
import com.rotunomp.app.SessionFactoryInstance;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        final ClientCredentials clientCredentials;
        try {
            clientCredentials = request.get();
            System.out.println("Successfully retrieved an access token! " + clientCredentials.getAccessToken());
            System.out.println("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");
            api.setAccessToken(clientCredentials.getAccessToken());

        } catch (IOException | WebApiException e) {
            e.printStackTrace();
            System.out.println("Error connecting to Spotify");
        }

        // Set up Hibernate environment
        sessionFactory = SessionFactoryInstance.getInstance();
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

    // Get list of albums and singles for a given artist
    public List<SimpleAlbum> getArtistsAlbums(String artistId) {
        Session session = sessionFactory.openSession();

        try {
            AlbumsForArtistRequest request =
                    api.getAlbumsForArtist(artistId)
                            .market("US")
                            .types(AlbumType.ALBUM, AlbumType.SINGLE)
                            .build();
            return request.get().getItems();
        } catch (WebApiException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String followArtist(String artistName, String userId) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Grab a list of artists that the user might be intending to follow
            // It's possible that the user intended to follow a different artist
            // Than the Spotify API thinks, so we want to account for that
            List<Artist> potentialArtists = searchArtistsByName(artistName, 3);
            String artistId = potentialArtists.get(0).getId();

            // TODO: Account for the user potentially not entering the artist they actually want

            // Get the artist from the DB. If the artist does not exist, create it
            FollowedArtist followedArtist = session.get(FollowedArtist.class, artistId);
            if (followedArtist == null) {
                followedArtist = new FollowedArtist();
                followedArtist.setId(artistId);
                session.save(followedArtist);
            }

            // Get the user from the DB. If the user does not exist, create it
            SpotifyUser user = session.get(SpotifyUser.class, userId);
            if (user == null) {
                user = new SpotifyUser();
                user.setId(userId);
                session.save(user);
            }

            // Add the user to the artist
            followedArtist.getFollowers().add(user);
            session.persist(followedArtist);

            tx.commit();

            return "Success adding " + artistName;

        } catch (ArtistNotFoundException | IOException e) {
            e.printStackTrace();
            if(tx != null) {
                session.getTransaction().rollback();
            }
        } finally {
            session.close();
        }

        return "Failure adding" + artistName;
    }

    public synchronized List<Artist> getArtistList(List<FollowedArtist> artists) {
        List<String> artistIds;
        // put all the artist ids into this new list
        artistIds = artists.stream().map(a -> a.getId()).collect(Collectors.toList());

        ArtistsRequest artistsRequest =
                api.getArtists(artistIds).build();
        try {
            return artistsRequest.get();
        } catch (IOException | WebApiException e) {
            e.printStackTrace();
        }

        return null;
    }
}
