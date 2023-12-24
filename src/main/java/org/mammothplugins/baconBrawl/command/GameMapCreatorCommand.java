package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.conversations.MapCreatorConversation;
import org.mammothplugins.baconBrawl.model.Game;
import org.mineacademy.fo.Messenger;

import java.util.List;

final class GameMapCreatorCommand extends GameSubCommand {

    GameMapCreatorCommand() {
        super("mapcreator/mc");

        this.setDescription("Change the MapCreator's name.");
        this.setPermission("baconbrawl.cmd.admin.mapcreator");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        Game game = PlayerCache.from(getPlayer()).getCurrentGame();

        if (game == null)
            Messenger.error(getPlayer(), "You must be editing a game to use this command!");
        else
            new MapCreatorConversation().show(getPlayer());
    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
