package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.model.Game;
import org.mineacademy.fo.Common;

import java.util.List;

final class GameListCommand extends GameSubCommand {

    public GameListCommand() {
        super("list");

        setDescription("Lists available games.");
        this.setPermission("baconbrawl.cmd.admin.list");
    }

    @Override
    protected void onCommand() {
        Common.tell(getPlayer(), "Ticks: " + getPlayer().getNoDamageTicks());
        tellInfo("Loaded games: " + Common.join(Game.getGames(), game -> game.getName() + " (" + game.getType().getName() + ")"));
    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
