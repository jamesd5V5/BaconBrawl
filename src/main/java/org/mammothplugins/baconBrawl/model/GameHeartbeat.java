package org.mammothplugins.baconBrawl.model;

import lombok.Getter;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.model.Countdown;

import java.util.Arrays;
import java.util.List;

public class GameHeartbeat extends Countdown {

    @Getter
    private final Game game;

    public GameHeartbeat(final Game game) {
        super(game.getGameDuration());

        this.game = game;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onTick() {
        final List<Integer> broadcastTimes = Arrays.asList(20, 30, 60);
        if (this.getTimeLeft() % 120 == 0 || this.getTimeLeft() <= 10 || broadcastTimes.contains(this.getTimeLeft()))
            this.game.broadcastWarn("Game ends in less than " + TimeUtil.formatTimeGeneric(this.getTimeLeft()) + ".");
    }

    @Override
    protected void onTickError(final Throwable t) {
        this.game.stop(GameStopReason.ERROR);
    }
    
    @Override
    protected void onEnd() {
        this.game.stop(GameStopReason.TIMEOUT);
    }
}
