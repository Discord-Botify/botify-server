package com.rotunomp.discordBot.app;

import com.rotunomp.discordBot.models.AppSession;
import com.rotunomp.discordBot.models.FollowedArtist;
import com.rotunomp.discordBot.models.AppUser;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class SessionFactoryInstance {

    private static SessionFactoryInstance sessionFactoryInstance;
    private static SessionFactory sessionFactory;

    public static SessionFactory getInstance() {
        if (sessionFactory == null) {
            sessionFactoryInstance = new SessionFactoryInstance();
        }
        return sessionFactory;
    }

    private SessionFactoryInstance() {
        // Set up the Hibernate environment
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(FollowedArtist.class);
        configuration.addAnnotatedClass(AppUser.class);
        configuration.addAnnotatedClass(AppSession.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }
}
