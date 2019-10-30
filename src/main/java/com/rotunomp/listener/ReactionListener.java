package com.rotunomp.listener;

import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {
    String artist;

    public ReactionListener() {
    }

    public void onPrivateMessageReactionEvent (PrivateMessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        System.out.println(event.getReaction().toString());

        switch (event.getReaction().toString()) {
            case "U+0031 U+20E3":
                break;
            case "U+0032 U+20E3":
                break;
            case "U+0033 U+20E3":
                break;
            default:
                break;
        }
    }
}
