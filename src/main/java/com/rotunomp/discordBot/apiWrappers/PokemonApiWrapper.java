package com.rotunomp.discordBot.apiWrappers;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PokemonApiWrapper {

    private CloseableHttpClient httpClient;
    private final String ROOT_URL = "https://pokeapi.co/api/v2/";
    private final String POKEMON = "pokemon/";

    public PokemonApiWrapper() {
        this.httpClient = HttpClients.createDefault();
    }

    public String getPokemonTypes(String name) {
        HttpGet httpGet = new HttpGet(ROOT_URL+POKEMON+name);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            StringBuffer type = new StringBuffer();

            JSONObject json = new JSONObject (EntityUtils.toString(httpResponse.getEntity()));
            for (Object obj : json.getJSONArray("types")) {
                JSONObject typeJson = (JSONObject) obj;
                type.append(typeJson.getJSONObject("type").get("name").toString().toUpperCase() + " ");
            }

            return type.toString();
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "null";
    }

    public String getFlavorText(String name) {
        HttpGet httpGet = new HttpGet(ROOT_URL+POKEMON+name);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);

            JSONObject json = new JSONObject (EntityUtils.toString(httpResponse.getEntity()));

            httpGet.setURI(new URI(json.getJSONObject("species").getString("url")));
            httpResponse = httpClient.execute(httpGet);

            json = new JSONObject (EntityUtils.toString(httpResponse.getEntity()));

            for (Object obj : json.getJSONArray("flavor_text_entries")) {
                JSONObject typeJson = (JSONObject) obj;
                if (typeJson.getJSONObject("language").get("name").equals("en")) {
                    return typeJson.getString("flavor_text");
                }
            }
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return "null";
    }
}
