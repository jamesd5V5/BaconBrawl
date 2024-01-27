package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameState;
import org.mammothplugins.baconBrawl.model.GameStopReason;

import java.util.List;

final class GameStopCommand extends GameSubCommand {

    GameStopCommand() {
        super("stop/st");

        this.setDescription("Force stop a game that is played.");
        this.setUsage("[name]");
        this.setPermission("baconbrawl.cmd.admin.stop");
    }

    @Override
    protected void onCommand() {
        Game game = this.findGameFromLocationOrFirstArg();

        this.checkBoolean(game.getState() != GameState.PREPLAYED, "Cannot stop the game until it starts!");
        this.checkBoolean(!game.isStopped(), "Can only stop non-stopped games!");
        game.stop(GameStopReason.COMMAND);
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}