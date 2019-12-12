package com.rotunomp.discordBot.services;

import com.neovisionaries.i18n.CountryCode;
import com.rotunomp.discordBot.app.Properties;
import com.rotunomp.discordBot.app.SessionFactoryInstance;
import com.rotunomp.discordBot.exceptions.ArtistNotFoundException;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.models.AppUser;
import com.rotunomp.discordBot.threads.TokenRefreshThread;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.enums.ModelObjectType;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.albums.GetSeveralAlbumsRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import com.wrapper.spotify.requests.data.follow.GetUsersFollowedArtistsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.query.Query;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class SpotifyService {

    private SpotifyApi spotifyApi;
    // This class is going to be a singleton
    private static SpotifyService serviceInstance = null;
    private SessionFactory sessionFactory;
    private CloseableHttpClient httpClient;


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

        // HttpClient for oauth
        this.httpClient = HttpClients.createDefault();
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

    public List<Artist> searchArtistsByName(String artistName, int listSize) throws ArtistNotFoundException, IOException {
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
                            .album_type("album,single")
                            .build();
            AlbumSimplified[] requestResponse = request.execute().getItems();
            List<AlbumSimplified> albums = new ArrayList<>();
            Collections.addAll(albums, requestResponse);
            List<AlbumSimplified> nextFiftyAlbums = Arrays.asList(requestResponse);
            int offset = 50;
            while (nextFiftyAlbums.size() == 50) {
                request =
                        spotifyApi.getArtistsAlbums(artistId)
                                .market(CountryCode.US)
                                .limit(50)
                                .offset(offset)
                                .album_type("album,single")
                                .build();
                offset += 50;
                nextFiftyAlbums = Arrays.asList(request.execute().getItems());
                albums.addAll(nextFiftyAlbums);
            }
            return albums;
        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Similar to getArtistsAlbums, this method returns a list of the more complete
    // Album object as opposed to the AlbumSimplified Object. This List is also sorted
    // by release date for album notification purposes
    public synchronized List<Album> getOrderedArtistReleases(String artistId) {
        List<Album> returnList = new ArrayList<>();

        // First we'll get the incomplete list of AlbumSimplified objects
        List<AlbumSimplified> simpleAlbums = getArtistsAlbums(artistId);

        // Set up our while loop variables
        int albumsRetrieved = 0;
        int albumsRemaining = simpleAlbums.size();
        List<String> albumIdsList = new ArrayList<>();
        for (AlbumSimplified albumSimplified : simpleAlbums) {
            albumIdsList.add(albumSimplified.getId());
        }

        // Loop that list
        while(albumsRemaining > 0) {
            int toGrabListSize;
            if(albumsRemaining < 20) {
                toGrabListSize = albumsRemaining;
                albumsRemaining = 0;
            } else {
                toGrabListSize = 20;
                albumsRemaining -= 20;
            }

            GetSeveralAlbumsRequest severalAlbumsRequest;
            String[] albumIdsArray =
                    albumIdsList.subList(albumsRetrieved, albumsRetrieved + toGrabListSize)
                            .toArray(new String[toGrabListSize]);
            severalAlbumsRequest = spotifyApi.getSeveralAlbums(
                    albumIdsArray
            ).build();

            try {
                Album[] fullAlbums = severalAlbumsRequest.execute();
                System.out.println("Array retrieved size: " + fullAlbums.length);
                for (Album album : fullAlbums) {
                    System.out.println("Adding this album to the returnList: " + album.getName());
                }
                Collections.addAll(returnList, fullAlbums);
            } catch (TooManyRequestsException e ) {
                int retryAfter = e.getRetryAfter();
                try {
                    Thread.sleep((retryAfter + 1) * 1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                getOrderedArtistReleases(artistId);
            } catch (IOException | SpotifyWebApiException e ) {
                e.printStackTrace();
            }

        }

        // Finally, sort the list by releaseDate
        System.out.println("Trying to sort this artist's albums: " + artistId);
        for (Album album : returnList) {
            System.out.println("Album in list: " + album.getName());
        }
        returnList.sort(new Comparator<Album>() {
            @Override
            public int compare(Album a1, Album a2) {
                // Turn the release dates of both albums into LocalDate objects
//            DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
//                    .parseLenient().parseCaseInsensitive()
//                    .parseDefaulting(ChronoField.YEAR_OF_ERA, 2016L)
//                    .appendPattern("[yyyy-MM-dd]")
//                    .appendPattern("[yyyy-MM]")
//                    .appendPattern("[yyyy]");
//            DateTimeFormatter formatter = builder.toFormatter(Locale.ENGLISH);
                LocalDate a1Release = null;
                LocalDate a2Release = null;
                try {
                    a1Release = LocalDate.parse(a1.getReleaseDate());
                    a2Release = LocalDate.parse(a2.getReleaseDate());
                    if (a1Release.equals(a2Release)) {
                        return 0;
                    }
                    return a1Release.isBefore(a2Release) ? -1 : 1;

                } catch (Exception e) {
                    System.err.println("Could not parse " + a1.getReleaseDate());
                    return 0;
                }
            }
        });


        return returnList;
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
            AppUser user = session.get(AppUser.class, userId);
            if (user == null) {
                user = new AppUser();
                user.setDiscordId(userId);
                session.save(user);
            }

            // Add the user to the artist
            followedArtist.getFollowers().add(user);
            session.persist(followedArtist);

            tx.commit();

            return "Success adding " + artistName;

        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                session.getTransaction().rollback();
            }
            followArtist(artistName, userId);
        } finally {
            session.close();
        }

        return "Failure adding" + artistName;
    }

    public String followArtistById(String artistId, String userId) throws InterruptedException {
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            // Get the artist's name from the Spotify API
            GetArtistRequest artistRequest = spotifyApi.getArtist(artistId).build();
            Artist artist = artistRequest.execute();
            String artistName = artist.getName();

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
            AppUser user = session.get(AppUser.class, userId);
            if (user == null) {
                user = new AppUser();
                user.setDiscordId(userId);
                session.save(user);
            }

            // Add the user to the artist
            followedArtist.getFollowers().add(user);
            session.persist(followedArtist);

            tx.commit();

            return "Success adding " + artistName;

        } catch (JDBCConnectionException e) {
            if (tx != null) {
                session.getTransaction().rollback();
            }
            // If the database timed out, call the method again
            int errorCode = e.getSQLException().getErrorCode();
            if (errorCode == 2013) {
                System.out.println("Lost connection to mySQL server, re-establishing connection...");
                // Restart the method
                return followArtistById(artistId, userId);
            } else {
                System.out.println("The error code was " + errorCode);
                e.printStackTrace();
            }
        } catch(TooManyRequestsException e) {
            System.out.println("Waiting for the rate limit to chill");
            int retryAfter = e.getRetryAfter();
            System.out.println("retrying after " + retryAfter + " seconds");
            Thread.sleep(retryAfter * 1000);
            followArtistById(artistId, userId);
        } catch(Exception e) {
            if (tx != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }

        return "Failure adding" + artistId;

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
            if (remainingArtists > 50) {
                String[] currentRequestIds = artistIds.subList(0, 50)
                        .toArray(new String[50]);

                artistsRequest =
                        spotifyApi.getSeveralArtists(currentRequestIds).build();
                artistIds = artistIds.subList(50, artistIds.size());
            } else {
                String[] currentRequestIds = artistIds.toArray(new String[artistIds.size()]);
                artistsRequest =
                        spotifyApi.getSeveralArtists(currentRequestIds).build();
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
        Set<FollowedArtist> followedArtists = getFollowedArtistsForDiscordUser(userId);
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
        Set<FollowedArtist> followedArtists = getFollowedArtistsForDiscordUser(userId);
        List<FollowedArtist> followedArtistList = new ArrayList<FollowedArtist>();

        for (FollowedArtist artist : followedArtists) {
            followedArtistList.add(artist);
        }

        Collections.sort(followedArtistList);

        return followedArtistList;
    }

    // Useful for the Discord unfollow interface, where artists need to
    // be displayed in sets of ten
    public List<List<FollowedArtist>> getFollowedArtistInTens(String userId) {
        List<FollowedArtist> bigArtistList = getFollowedArtistsListForDiscordId(userId);
        List<List<FollowedArtist>> returnList = new ArrayList<>();

        // Get the size of the big list
        int remainingArtists = bigArtistList.size();
        final int pageSize = 10;

        for (int i = 0; i < bigArtistList.size(); i += pageSize) {
            if (i + pageSize < bigArtistList.size())
                returnList.add(bigArtistList.subList(i, i + pageSize));
            else
                returnList.add(bigArtistList.subList(i, bigArtistList.size()));
        }

        return returnList;

    }

    public Set<FollowedArtist> getFollowedArtistsForDiscordUser(String userId) {
        Session session = sessionFactory.openSession();
        Set<FollowedArtist> followedArtists = null;

        try {
            followedArtists = session.get(AppUser.class, userId).getFollowedArtists();
        } catch (JDBCConnectionException e) {
            // If the database timed out, call the method again
            int errorCode = e.getSQLException().getErrorCode();
            if (errorCode == 2013) {
                System.out.println("Lost connection to mySQL server, re-establishing connection...");
                return getFollowedArtistsForDiscordUser(userId);
            } else {
                System.out.println("The error code was " + errorCode);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return followedArtists;
    }

    public String unfollowArtist(String artistId, String userId) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        String artistName = null;

        try {
            // First get the user from the database
            AppUser user = session.get(AppUser.class, userId);

            // Then get the artist from the database
            FollowedArtist artist = session.get(FollowedArtist.class, artistId);
            artistName = artist.getName();

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
        } catch (JDBCException e) {
            if (tx != null) {
                session.getTransaction().rollback();
            }
            // If the database timed out, call the method again
            int errorCode = e.getSQLException().getErrorCode();
            if (errorCode == 2013) {
                System.out.println("Lost connection to mySQL server, re-establishing connection...");
                // TODO: Restart the method
            } else {
                System.out.println("The error code was " + errorCode);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                session.getTransaction().rollback();
            }
        } finally {
            session.close();
        }

        return "Successfully unfollowed " + artistName;
    }

    public List<FollowedArtist> getAllDatabaseArtists() {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery("FROM FollowedArtist").list();
        } catch (JDBCConnectionException e) {
            // If the database timed out, call the method again
            int errorCode = e.getSQLException().getErrorCode();
            if (errorCode == 2013) {
                System.out.println("Lost connection to mySQL server, re-establishing connection...");
                return getAllDatabaseArtists();
            } else {
                System.out.println("The error code was " + errorCode);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return null;
    }

    // Exchange for oauth tokens with Spotify
    public JSONObject exchangeCodeForTokens(String code) throws IOException {
        HttpPost httpPost = new HttpPost("https://accounts.spotify.com/api/token");
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpPost.setEntity(getSpotifyTokenExchangeParams(code));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        return new JSONObject(EntityUtils.toString(response.getEntity()));
    }

    // Helper to build body for token exchange
    private HttpEntity getSpotifyTokenExchangeParams(String code) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", Properties.get("spotify_client_id")));
        params.add(new BasicNameValuePair("client_secret", Properties.get("spotify_client_secret")));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("code", code));
        params.add(new BasicNameValuePair("redirect_uri", "https://botify.michaelrotuno.dev/spotify-oauth"));

        return new UrlEncodedFormEntity(params, Consts.UTF_8);
    }

    // Use an access token to get a user's Spotify information
    public JSONObject getUsersSpotifyInfo(String accessToken) throws IOException {
        HttpPost httpPost = new HttpPost("https://api.spotify.com/v1/me");
        httpPost.setHeader("Authorization", "Bearer " + accessToken);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        return new JSONObject(EntityUtils.toString(response.getEntity()));
    }

    // Use an access token to get user info with the Spotify Wrapper
    public User getUsersSpotifyInfo2(String accessToken) throws IOException, SpotifyWebApiException {
        SpotifyApi temporaryApi = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .build();
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = temporaryApi.getCurrentUsersProfile()
                .build();

        return getCurrentUsersProfileRequest.execute();
    }

    // This method grabs all of the followed artists from the Spotify API
    // for a particular user
    public List<FollowedArtist> followAllArtistsFollowedOnSpotify(AppUser appUser) throws IOException, SpotifyWebApiException {
        // Start by getting a new access token for the AppUser
        String accessToken = refreshSpotifyToken(appUser.getSpotifyRefreshToken());

        // Use that access token to grab the user's followed artists on Spotify
        List<Artist> spotifyFollowedArtists = getUsersSpotifyFollowedArtists(accessToken);

        // Save all those artists in the database
        followAllArtists(spotifyFollowedArtists, appUser.getDiscordId());

        return null;
    }

    // Get a new Spotify access token from the Spotify API
    private String refreshSpotifyToken(String refreshToken) throws IOException {
        HttpPost httpPost = new HttpPost("https://accounts.spotify.com/api/token");

        // Set the url parameters
        httpPost.setEntity(getSpotifyRefreshExchangeParams(refreshToken));

        // TODO: We might need to set the client ID and secret in an authorization header
        // in base64 encoding. Currently we have it as a url param but not certain it works

        // Get the access token
        CloseableHttpResponse response = httpClient.execute(httpPost);
        JSONObject responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
        String accessToken = responseBody.getString("access_token");
        return accessToken;
    }

    private HttpEntity getSpotifyRefreshExchangeParams(String refreshToken) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", Properties.get("spotify_client_id")));
        params.add(new BasicNameValuePair("client_secret", Properties.get("spotify_client_secret")));
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));

        return new UrlEncodedFormEntity(params, Consts.UTF_8);
    }


    private List<Artist> getUsersSpotifyFollowedArtists(String accessToken) throws IOException, SpotifyWebApiException {
        // It's just easier to use the API wrapper for this call
        SpotifyApi temporaryApi = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .build();

        // Build our return list, 50 at a time
        List<Artist> returnList = new ArrayList<>();
        String lastArtistId = "";
        boolean hasMoreArtists = true;

        while (hasMoreArtists) {
            GetUsersFollowedArtistsRequest getUsersFollowedArtistsRequest = null;
            if (lastArtistId.equals("")) {
                getUsersFollowedArtistsRequest = temporaryApi
                        .getUsersFollowedArtists(ModelObjectType.ARTIST)
                        .limit(50)
                        .build();
            } else {
                getUsersFollowedArtistsRequest = temporaryApi
                        .getUsersFollowedArtists(ModelObjectType.ARTIST)
                        .after(lastArtistId)
                        .limit(50)
                        .build();
            }

            Artist[] artists = getUsersFollowedArtistsRequest.execute().getItems();
            // If the return list is of size 50, we need to get the next batch
            // Otherwise we can leave the loop
            if (artists.length < 50) {
                hasMoreArtists = false;
            } else {
                lastArtistId = artists[49].getId();
            }
            returnList.addAll(Arrays.asList(artists));

        }

        return returnList;

    }

    private void followAllArtists(List<Artist> artists, String discordId) {
        for (Artist artist : artists) {
            try {
                followArtistById(artist.getId(), discordId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
