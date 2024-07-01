package org.mammothplugins.ssm.command;

import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.conversations.MapCreatorConversation;
import org.mammothplugins.ssm.model.Game;
import org.mineacademy.fo.Messenger;

import java.util.List;

final class GameMapCreatorCommand extends GameSubCommand {

    GameMapCreatorCommand() {
        super("mapcreator/mc");

        this.setDescription("Change the MapCreator's name.");
        this.setPermission("ssm.cmd.admin.mapcreator");
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
