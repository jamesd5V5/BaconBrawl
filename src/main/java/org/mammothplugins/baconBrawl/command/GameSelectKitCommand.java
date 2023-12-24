package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.menu.KitSelectorMenu;
import org.mammothplugins.baconBrawl.model.Game;
import org.mineacademy.fo.ItemUtil;

import java.util.List;

final class GameSelectKitCommand extends GameSubCommand {

    GameSelectKitCommand() {
        super("menu/m");

        this.setDescription("Select a Kit");
        this.setPermission("baconbrawl.cmd.player.menu");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();
        Game game = this.findGameFromLocationOrFirstArg();

        this.checkBoolean(game.isLobby(), "Can only change kits in Lobby! "
                + game.getName() + " is " + ItemUtil.bountifyCapitalized(game.getState()).toLowerCase() + ".");
        (new KitSelectorMenu(getPlayer())).displayTo(this.getPlayer());
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}