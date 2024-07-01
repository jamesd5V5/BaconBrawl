package org.mammothplugins.ssm.command;

import org.mammothplugins.ssm.model.Game;
import org.mineacademy.fo.Common;

import java.util.List;

final class GameListCommand extends GameSubCommand {

    public GameListCommand() {
        super("list");

        setDescription("Lists available games.");
        this.setPermission("ssm.cmd.admin.list");
    }

    @Override
    protected void onCommand() {
//        String s = PlaceholderAPI.setPlaceholders(getPlayer(), "Okat: Games Won: %ssm_gamesWon% Games Played: %ssm_gamesPlayed% +  Ratio: %ssm_gameRatio% ");
//        Common.tell(getPlayer(), s);
        tellInfo("Loaded games: " + Common.join(Game.getGames(), game -> game.getName() + " (" + game.getType().getName() + ")"));
    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
