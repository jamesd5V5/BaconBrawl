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
//        String s = PlaceholderAPI.setPlaceholders(getPlayer(), "Okat: Games Won: %baconbrawl_gamesWon% Games Played: %baconbrawl_gamesPlayed% +  Ratio: %baconbrawl_gameRatio% ");
//        Common.tell(getPlayer(), s);
        tellInfo("Loaded games: " + Common.join(Game.getGames(), game -> game.getName() + " (" + game.getType().getName() + ")"));
    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
