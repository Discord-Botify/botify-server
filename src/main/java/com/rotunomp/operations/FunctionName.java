package com.rotunomp.operations;

public enum FunctionName {
    PING ("!ping"),
    ADD_ROLE ("!add-role"),
    REMOVE_ROLE ("!remove-role"),
    POKEMON ("!pokemon"),
    SPOTIFY ("!spotify");

    public final String command;

    FunctionName(String command) {
        this.command = command;
    }
}
