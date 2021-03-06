package com.rotunomp.discordBot.models;

import javax.persistence.*;

import com.google.gson.annotations.Expose;

import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;

@Entity
@Table(name = "AppSession", catalog = "discord_bot", uniqueConstraints = {
        @UniqueConstraint(columnNames = "session_id")})
public class AppSession {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "session_id")
    @Expose
    private String sessionId;
    @Expose
    private String discordId;
    private LocalDate lastTimeUsed;

    public AppSession() {}

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public LocalDate getLastTimeUsed() {
        return lastTimeUsed;
    }

    public void setLastTimeUsed(LocalDate lastTimeUsed) {
        this.lastTimeUsed = lastTimeUsed;
    }
}