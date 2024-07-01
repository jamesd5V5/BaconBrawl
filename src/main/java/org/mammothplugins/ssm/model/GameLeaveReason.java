package org.mammothplugins.ssm.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum GameLeaveReason {

    GAME_STOP,
    QUIT_SERVER,
    COMMAND,
    ERROR,
    ESCAPED;

    private boolean autoSpectateOnLeave = false;

    public boolean autoSpectateOnLeave() {
        return this.autoSpectateOnLeave;
    }
}
