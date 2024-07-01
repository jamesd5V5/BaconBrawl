package org.mammothplugins.ssm.command;

import org.mammothplugins.ssm.model.Game;
import org.mammothplugins.ssm.model.GameState;
import org.mammothplugins.ssm.model.GameStopReason;

import java.util.List;

final class GameStopCommand extends GameSubCommand {

    GameStopCommand() {
        super("stop/st");

        this.setDescription("Force stop a game that is played.");
        this.setUsage("[name]");
        this.setPermission("ssm.cmd.admin.stop");
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