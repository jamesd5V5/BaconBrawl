package org.mammothplugins.baconBrawl.model;

public enum GameState {

    STOPPED,
    LOBBY,
    PLAYED,
    EDITED;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
