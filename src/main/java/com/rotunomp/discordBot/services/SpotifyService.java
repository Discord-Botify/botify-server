package com.rotunomp.discordBot.services;

import com.neovisionaries.i18n.CountryCode;
import com.rotunomp.discordBot.app.Properties;
import com.rotunomp.discordBot.app.SessionFactoryInstance;
import com.rotunomp.discordBot.exceptions.ArtistNotFoundException;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.models.SpotifyUser;
import com.rotunomp.discordBot.threads.TokenRefreshThread;
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
import org.hibernate.query.Query;

import java.io.IOException;
import java.util.*;
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

        // Begin the token refresh thread
        TokenRefreshThread refreshThread = new TokenRefreshThread(spotifyApi);
        refreshThread.start();
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

    // Get list of albums for a given artist
    public synchronized List<AlbumSimplified> getArtistsAlbums(String artistId) {
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

    public String getFollowedArtistStringForUser(String userId) {
        Set<FollowedArtist> followedArtists= getFollowedArtistsForDiscordUser(userId);
        StringBuilder artists = new StringBuilder();
        for (FollowedArtist followedArtist : followedArtists) {
            artists.append(followedArtist.getName())
            .append(" | ID: ")
            .append(followedArtist.getId())
            .append("\n");
        }

        return artists.toString();
    }

    public List<FollowedArtist> getFollowedArtistsListForDiscordId(String userId) {
        Set<FollowedArtist> followedArtists= getFollowedArtistsForDiscordUser(userId);
        List<FollowedArtist> followedArtistList = new ArrayList<FollowedArtist>();

        for (FollowedArtist artist : followedArtists) {
            followedArtistList.add(artist);
        }

        Collections.sort(followedArtistList);

        return followedArtistList;
    }

    public List<FollowedArtist> getFollowedArtistsListForSpotifyId(String userId) {
        // Get the corresponding user in the database
        Session session = sessionFactory.openSession();
        Query query = sessionFactory.getCurrentSession().createQuery("from SpotifyUser where spotifyId = :spotifyId");
        query.setParameter("spotifyId", userId);
        SpotifyUser user = (SpotifyUser) query.list().get(0);

        // Call the method to get the list of artists based on Discord ID
        return getFollowedArtistsListForDiscordId(user.getId());
    }

    public List<List<FollowedArtist>> getFollowedArtistInTens(String userId) {
        List<FollowedArtist> bigArtistList= getFollowedArtistsListForDiscordId(userId);
        List<List<FollowedArtist>> returnList = new ArrayList<>();

        // Get the size of the big list
        int remainingArtists = bigArtistList.size();
        final int pageSize = 10;

        for (int i = 0; i < bigArtistList.size(); i += pageSize) {
            if (i + pageSize < bigArtistList.size())
                returnList.add(bigArtistList.subList(i,i + pageSize));
            else
                returnList.add(bigArtistList.subList(i, bigArtistList.size()));
        }

        return returnList;

    }

    public Set<FollowedArtist> getFollowedArtistsForDiscordUser(String userId) {
        Session session = sessionFactory.openSession();
        Set<FollowedArtist> followedArtists = session.get(SpotifyUser.class, userId).getFollowedArtists();
        session.close();
        return followedArtists;
    }

    public String unfollowArtist(String artistId, String userId) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // First get the user from the database
        SpotifyUser user = session.get(SpotifyUser.class, userId);

        // Then get the artist from the database
        FollowedArtist artist = session.get(FollowedArtist.class, artistId);
        String artistName = artist.getName();

        // Delete the artist from the user's following set
        user.getFollowedArtists().remove(artist);
        // Delete the user from the artist's followed set
        artist.getFollowers().remove(user);

        session.persist(artist);
        session.persist(user);

        if (artist.getFollowers().size() == 0) {
            session.delete(artist);
        }

        tx.commit();
        session.close();

        return "Successfully unfollowed " + artistName;
    }

    public List<FollowedArtist> getAllDatabaseArtists() {
        Session session = sessionFactory.openSession();
        return session.createQuery("FROM FollowedArtist").list();
    }
}
