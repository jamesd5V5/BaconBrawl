package org.mammothplugins.baconBrawl.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum GameLeaveReason {

    GAME_STOP,
    QUIT_SERVER,
    COMMAND,
    ERROR,
    OBJECTIVE_LOST(true),
    ESCAPED;

    private boolean autoSpectateOnLeave = false;

    public boolean autoSpectateOnLeave() {
        return this.autoSpectateOnLeave;
    }
}
