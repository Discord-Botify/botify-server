package com.rotunomp.discordBot.app;

public enum StatusResponse {
    SUCCESS ("Success"),
    ERROR ("Error");

    public String status;

    private StatusResponse(String status) {
        this.status = status;
    }
}
