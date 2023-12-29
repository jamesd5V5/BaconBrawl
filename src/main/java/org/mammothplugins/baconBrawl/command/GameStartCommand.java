package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mineacademy.fo.ItemUtil;

import java.util.List;

final class GameStartCommand extends GameSubCommand {

    GameStartCommand() {
        super("start/s");

        this.setDescription("Force start a game in lobby.");
        this.setUsage("[name]");
        this.setPermission("baconbrawl.cmd.admin.start");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();
        Game game = this.findGameFromLocationOrFirstArg();


        this.checkBoolean(game.isLobby(), "Can only start games in lobby! "
                + game.getName() + " is " + ItemUtil.bountifyCapitalized(game.getState()).toLowerCase() + ".");
        this.checkBoolean(game.getPlayers(GameJoinMode.PLAYING).size() >= game.getMinPlayers(), "Not enough players! "
                + game.getName() + " needs " + game.getMinPlayers() + " players.");
        game.start();
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}