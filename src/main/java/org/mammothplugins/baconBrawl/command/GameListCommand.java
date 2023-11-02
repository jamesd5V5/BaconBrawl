package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.model.Game;
import org.mineacademy.fo.Common;

import java.util.List;

final class GameListCommand extends GameSubCommand {

    public GameListCommand() {
        super("list");

        setDescription("Lists available games.");
    }

    @Override
    protected void onCommand() {
        tellInfo("Loaded games: " + Common.join(Game.getGames(), game -> game.getName() + " (" + game.getType().getName() + ")"));
    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
