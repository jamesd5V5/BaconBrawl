package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameStopReason;

import java.util.List;

final class GameStopCommand extends GameSubCommand {

    GameStopCommand() {
        super("stop/st");

        this.setDescription("Force stop a game that is played.");
        this.setUsage("[name]");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();
        Game game = this.findGameFromLocationOrFirstArg();

        this.checkBoolean(!game.isStopped(), "Can only stop non-stopped games!");
        game.stop(GameStopReason.COMMAND);
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}