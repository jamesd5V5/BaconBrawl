package org.mammothplugins.baconBrawl.model;

public enum GameState {

    STOPPED,
    LOBBY,
    PREPLAYED,
    PLAYED,
    POSTPLAYED,
    EDITED;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
