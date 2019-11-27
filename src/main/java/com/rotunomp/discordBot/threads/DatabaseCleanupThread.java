package com.rotunomp.discordBot.threads;

import com.rotunomp.discordBot.app.SessionFactoryInstance;
import com.rotunomp.discordBot.models.AppSession;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DatabaseCleanupThread extends Thread {

    private SessionFactory sessionFactory;

    public DatabaseCleanupThread() {
        this.sessionFactory = SessionFactoryInstance.getInstance();
    }

    public void run() {
        LocalDateTime timeSinceLastUpdate = LocalDateTime.now().minusDays(1);
        while (true) {
            LocalDateTime currentTime = LocalDateTime.now();
            if(timeSinceLastUpdate.isBefore(currentTime.minusDays(1))) {
                System.out.println("Beginning the database cleanup process");
                startDatabaseCleanup();
                timeSinceLastUpdate = currentTime;
            }
            // Sleep the thread for a while to reduce compute power
            try {
                // 1000 seconds
                sleep(1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void startDatabaseCleanup() {
        // Grab all the App Sessions
        Session hibernateSession = sessionFactory.openSession();
        hibernateSession.beginTransaction();
        List<AppSession> appSessions =
                hibernateSession.createQuery("FROM AppSession").list();

        // Delete any AppSessions that are x days old
        int daysOldRequiredToDelete = 30;    // x
        LocalDate now = LocalDate.now();
        for (AppSession appSession : appSessions) {
            if(appSession.getLastTimeUsed().isBefore(now.minusDays(daysOldRequiredToDelete))) {
                hibernateSession.delete(appSession);
            }
        }

        hibernateSession.getTransaction().commit();
        hibernateSession.close();
    }

}
