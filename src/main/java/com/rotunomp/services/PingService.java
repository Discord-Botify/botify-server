package com.rotunomp.services;

import net.dv8tion.jda.api.entities.MessageChannel;

public class PingService extends Thread {
    private MessageChannel channel;
    private int time = -1;

    public PingService(MessageChannel channel) {
        this.channel = channel;
    }

    public PingService(MessageChannel channel, int time) {
        this.channel = channel;
        this.time = time;
    }

    public void run() {
        if (time == -1) {
            try {
                sleep(10000);
                channel.sendMessage("Pongsplosion!").queue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                sleep(time);
                channel.sendMessage("Pongsplosion!").queue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
