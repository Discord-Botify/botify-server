package com.rotunomp.apiWrappers;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class PokemonApiWrapper {

    private CloseableHttpClient httpClient;
    private final String ROOT_URL = "https://pokeapi.co/api/v2/";
    private final String POKEMON = "pokemon/";

    public PokemonApiWrapper() {
        this.httpClient = HttpClients.createDefault();
    }

    public String getPokemonTypes() {
        return null;
    }

//    public String getFlavorText(String pokemon) {
//        try {
//            HttpGet request = new HttpGet();
//        }
//    }
}
