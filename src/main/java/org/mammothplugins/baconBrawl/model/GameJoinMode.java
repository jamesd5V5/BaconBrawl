package org.mammothplugins.baconBrawl.model;

public enum GameJoinMode {

    PLAYING,
    EDITING,
    SPECTATING;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
