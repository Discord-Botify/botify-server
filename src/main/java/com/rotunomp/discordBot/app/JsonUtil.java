package com.rotunomp.discordBot.app;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.models.SpotifyUser;
import spark.ResponseTransformer;

import java.util.HashSet;
import java.util.Set;

public class JsonUtil {

    public static ResponseTransformer json() {
        return JsonUtil::toJson;
    }

    public static ResponseTransformer jsonWithExposeAnnotation() {
        return JsonUtil::toJsonWithExposeAnnotation;
    }

    public static ResponseTransformer jsonWithoutSpotifyUser() {
        return JsonUtil::toJsonExcludeSpotifyUser;
    }

    public static ResponseTransformer jsonWithoutFollowedArtist() {
        return JsonUtil::toJsonExcludeArtist;
    }

    private static String toJson(Object object) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
        return gson.toJson(object);
    }

    private static String toJsonExcludeSpotifyUser(Object object) {
        return excludeClass(object, SpotifyUser.class);
    }

    private static String toJsonExcludeArtist(Object object) {
        return excludeClass(object, FollowedArtist.class);
    }

    private static String toJsonWithExposeAnnotation(Object object) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson.toJson(object);
    }

    private static String excludeClass(Object object, Class<?> classToExclude) {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {

                    public boolean shouldSkipClass(Class<?> clazz) {
                        return (clazz == classToExclude);
                    }

                    /**
                     * Custom field exclusion goes here
                     */
                    public boolean shouldSkipField(FieldAttributes f) {
                        return false;
                    }

                })
                /**
                 * Use serializeNulls method if you want To serialize null values
                 * By default, Gson does not serialize null values
                 */
                .serializeNulls()
                .create();
        return gson.toJson(object);

    }

}