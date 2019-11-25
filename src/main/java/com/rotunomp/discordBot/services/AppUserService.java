package com.rotunomp.discordBot.services;

import com.rotunomp.discordBot.app.SessionFactoryInstance;
import com.rotunomp.discordBot.models.AppUser;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class AppUserService {

    private static AppUserService serviceInstance;
    private SessionFactory sessionFactory;

    public static AppUserService getInstance() {
        if (serviceInstance == null) {
            serviceInstance = new AppUserService();
        }
        return serviceInstance;
    }

    private AppUserService() {
        // Set up Hibernate environment
        sessionFactory = SessionFactoryInstance.getInstance();

    }

    public AppUser getAppUserWithDiscordId(String discordId) {
        Session session = sessionFactory.openSession();
        return session.get(AppUser.class, discordId);
    }

    // Put a user's Spotify information in the AppUser object
    public void saveSpotifyInformation(
            String discordId,
            String spotifyId,
            String spotifyUserName,
            String spotifyRefreshToken) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        AppUser appUser = session.get(AppUser.class, discordId);
        appUser.setSpotifyId(spotifyId);
        appUser.setSpotifyUserName(spotifyUserName);
        appUser.setSpotifyRefreshToken(spotifyRefreshToken);
        session.update(appUser);
        session.getTransaction().commit();
        session.close();
    }

}
