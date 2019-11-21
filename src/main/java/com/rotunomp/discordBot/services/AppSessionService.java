package com.rotunomp.discordBot.services;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.rotunomp.discordBot.app.SessionFactoryInstance;
import com.rotunomp.discordBot.models.AppSession;

public class AppSessionService {

    private SessionFactory sessionFactory;
    private static AppSessionService appSessionService;

    // Get instance
    public static AppSessionService getInstance() {
        if (appSessionService == null) {
            appSessionService = new AppSessionService();
        }
        return appSessionService;
    }

    private AppSessionService() {
        this.sessionFactory = SessionFactoryInstance.getInstance();
    }

    // Start a new app session, which is a session id mapped to a discord id
    public String startAppSession(String discordId) {
        Session hibernateSession = sessionFactory.openSession();

        AppSession appSession = new AppSession();
        appSession.setDiscordId(discordId);
        hibernateSession.persist(appSession);

        return appSession.getSessionId();
    }

    // TODO delete a session based on ID

}