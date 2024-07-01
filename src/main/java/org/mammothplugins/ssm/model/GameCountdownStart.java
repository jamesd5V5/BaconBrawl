package org.mammothplugins.ssm.model;

import lombok.Getter;
import org.mammothplugins.ssm.PlayerCache;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.model.Countdown;
import org.mineacademy.fo.remain.CompSound;

public class GameCountdownStart extends Countdown {

    @Getter
    private final Game game;

    protected GameCountdownStart(final Game game) {
        super(game.getLobbyDuration());

        this.game = game;
    }

    @Override
    protected void onTick() {
        if (this.getTimeLeft() <= 5 || this.getTimeLeft() % 10 == 0) {
            this.game.broadcastWarn("Game starts in less than " + Common.plural(this.getTimeLeft(), "second"));
            for (PlayerCache cache : game.getPlayersInAllModes())
                CompSound.NOTE_PIANO.play(cache.toPlayer());
        }
    }

    @Override
    protected void onTickError(final Throwable t) {
        this.game.stop(GameStopReason.ERROR);
    }

    @Override
    protected void onEnd() {
        this.game.start();
    }
}
