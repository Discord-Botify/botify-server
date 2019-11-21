package com.rotunomp.discordBot.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rotunomp.discordBot.app.SessionFactoryInstance;
import com.rotunomp.discordBot.models.SpotifyUser;
import com.rotunomp.discordBot.app.Properties;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * DiscordService
 */
public class DiscordService {
    private final String API_TOKEN = "https://discordapp.com/api/v6/oauth2/token";
    private final String API_USER = "https://discordapp.com/api/v6/users/@me";
    private final String CLIENT_ID = Properties.get("discord_client_id");
    private final String CLIENT_SECRET = Properties.get("discord_client_secret");
    private final String REDIRECT_URI = "https://botify.michaelrotuno.dev/oauth";

    private CloseableHttpClient httpClient;
    private SessionFactory sessionFactory;
    private static DiscordService serviceInstance;

    // Get singleton
    public static DiscordService getInstance() {
        if(serviceInstance == null) {
            serviceInstance = new DiscordService();
        }
        return serviceInstance;
    }

    private DiscordService() {
        this.httpClient = HttpClients.createDefault();
        this.sessionFactory = SessionFactoryInstance.getInstance();
    }

    // The main logic for signing in a user. In this method, we
    // send the authorization code to Discord to obtain an access token,
    // we get the user's information, and pass it back as a JSONObject
    public JSONObject loginUser(String code) throws IOException {
        JSONObject returnJson = null;

        // Get the access token and refresh token
        JSONObject tokenResponseJson = exchangeCodeForTokens(code);

	    System.out.println("Token response: " + tokenResponseJson);

        String accessToken = tokenResponseJson.getString("access_token");
        String refreshToken = tokenResponseJson.getString("refresh_token");


        // Get the user's information from the Discord API
        JSONObject discordUserResponseJson = getDiscordUserInfo(accessToken);
        String discordId = discordUserResponseJson.getString("id");


        // Check if the user exists in the database, and if it doesn't create an entry
        SpotifyUser user = getOrCreateUser(discordId);


        // Pass the user's info back to the front end
        return discordUserResponseJson;
    }

    // Build the params needed to exchange for a token with Discord
    private UrlEncodedFormEntity getDiscordTokenExchangeParams(String code) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", CLIENT_ID));
        params.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("code", code));
	    params.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
        params.add(new BasicNameValuePair("scope", "identify"));

        return new UrlEncodedFormEntity(params, Consts.UTF_8);
    }

    // Exchange code for access token and refresh token with the Discord API
    private JSONObject exchangeCodeForTokens(String code) throws ClientProtocolException, IOException {
        HttpPost httpPost = new HttpPost(API_TOKEN);
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpPost.setEntity(getDiscordTokenExchangeParams(code));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        JSONObject tokenResponseJson = new JSONObject (EntityUtils.toString(response.getEntity()));

        return tokenResponseJson;
    }
    
    // Get a Discord user's info from the API with their access token
    private JSONObject getDiscordUserInfo(String accessToken) throws ClientProtocolException, IOException {
        JSONObject returnBody = null;

        HttpGet httpGet = new HttpGet(API_USER);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);
        HttpResponse response = httpClient.execute(httpGet);
        
        return new JSONObject(EntityUtils.toString(response.getEntity()));
    }

    private SpotifyUser getOrCreateUser(String discordId) {
        // Start a hibernate session
        Session session = sessionFactory.openSession();

        // See if a user exists, and if not add it
        SpotifyUser user = session.get(SpotifyUser.class, discordId);
        if (user == null) {
            user = new SpotifyUser();
            user.setId(discordId);
            session.persist(user);
        }

        return user;
    }
}
