package com.rotunomp.services;

import com.neovisionaries.i18n.CountryCode;
import com.rotunomp.app.Properties;
import com.rotunomp.app.SessionFactoryInstance;
import com.rotunomp.exceptions.ArtistNotFoundException;
import com.rotunomp.models.FollowedArtist;
import com.rotunomp.models.SpotifyUser;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpotifyService {

    private SpotifyApi spotifyApi;
    // This class is going to be a singleton
    private static SpotifyService serviceInstance = null;
    private SessionFactory sessionFactory;

    // Returns the singleton
    public static SpotifyService getService() {
        if (serviceInstance == null) {
            serviceInstance = new SpotifyService();
        }
        return serviceInstance;
    }

    private SpotifyService() {
        System.out.println("Constructing a new SpotifyService");
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(Properties.get("spotify_client_id"))
                .setClientSecret(Properties.get("spotify_client_secret"))
                .build();
        /* Create a request object. */
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

        // Set up Hibernate environment
        sessionFactory = SessionFactoryInstance.getInstance();
    }

    public String getAlbumNameById(String albumId) {
        GetAlbumRequest request = spotifyApi.getAlbum(albumId).build();
        try {
            Album album = request.execute();
            return album.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Cannot find album: " + albumId;
    }

    private List<Artist> searchArtistsByName(String artistName, int listSize) throws ArtistNotFoundException, IOException {
        SearchArtistsRequest request = spotifyApi.searchArtists(artistName).limit(listSize).build();
        try {
            return Arrays.asList(request.execute().getItems());
        } catch (SpotifyWebApiException e) {
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
    public List<AlbumSimplified> getArtistsAlbums(String artistId) {
        Session session = sessionFactory.openSession();

        try {
            GetArtistsAlbumsRequest request =
                    spotifyApi.getArtistsAlbums(artistId)
                            .market(CountryCode.US)
                            .limit(50)
                            .album_type("album")
                            .build();
            return Arrays.asList(request.execute().getItems());
        } catch (IOException | SpotifyWebApiException e) {
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
            Artist apiArtist = null;

            // If the user puts in an artist that doesn't exist, return this string
            try {
                apiArtist = potentialArtists.get(0);
            } catch (Exception e) {
                return "Could not find " + artistName;
            }
            String artistId = apiArtist.getId();
            artistName = apiArtist.getName();

            // TODO: Account for the user potentially not entering the artist they actually want

            // Get the artist from the DB. If the artist does not exist, create it
            FollowedArtist followedArtist = session.get(FollowedArtist.class, artistId);
            if (followedArtist == null) {
                followedArtist = new FollowedArtist();
                followedArtist.setId(artistId);
                followedArtist.setName(artistName);
                followedArtist.setAlbumCount(getArtistsAlbums(artistId).size());
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

        } catch (Exception e) {
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

        // The api is only able to process lists of 50 or less. We need to paginate the
        // List of artist ids to make batch requests of 50 maximum
        int remainingArtists = artistIds.size();
        List<Artist> returnList = new ArrayList<>();
        while (remainingArtists > 0) {
            GetSeveralArtistsRequest artistsRequest = null;
            if(remainingArtists > 50) {
                artistsRequest =
                        spotifyApi.getSeveralArtists(
                                artistIds.subList(0, 50)
                                        .toArray(new String[artistIds.size()])).build();
                artistIds = artistIds.subList(50, artistIds.size());
            } else {
                artistsRequest =
                        spotifyApi.getSeveralArtists(artistIds.toArray(new String[artistIds.size()])).build();
            }

            try {
                returnList.addAll(Arrays.asList(artistsRequest.execute()));
            } catch (IOException | SpotifyWebApiException e) {
                e.printStackTrace();
            }

            remainingArtists -= 50;
        }

        return returnList;
    }
}
