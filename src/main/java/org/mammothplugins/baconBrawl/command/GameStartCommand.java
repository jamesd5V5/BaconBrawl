package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.model.Game;
import org.mineacademy.fo.ItemUtil;

import java.util.List;

final class GameStartCommand extends GameSubCommand {

    GameStartCommand() {
        super("start/s");

        this.setDescription("Force start a game in lobby.");
        this.setUsage("[name]");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();
        Game game = this.findGameFromLocationOrFirstArg();


        this.checkBoolean(game.isLobby(), "Can only start games in lobby! "
                + game.getName() + " is " + ItemUtil.bountifyCapitalized(game.getState()).toLowerCase() + ".");

        game.start();
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}