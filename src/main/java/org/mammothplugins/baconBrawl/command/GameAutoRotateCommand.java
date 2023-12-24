package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.Game;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.remain.CompSound;

import java.util.List;

final class GameAutoRotateCommand extends GameSubCommand {

    GameAutoRotateCommand() {
        super("autorotate/ar");

        this.setDescription("Set whether the game should auto rotate.");
        this.setPermission("baconbrawl.cmd.admin.autorotate");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        Game game = PlayerCache.from(getPlayer()).getCurrentGame();

        if (game == null)
            Messenger.error(getPlayer(), "You must be editing a game to use this command!");
        else {
            if (game.isAutoRotate())
                game.setAutoRotate(false);
            else
                game.setAutoRotate(true);
            CompSound.CAT_MEOW.play(getPlayer(), 5.0F, 0.3F);
            Common.tell(getPlayer(), "&7AutoRotate has been set to " + (game.isAutoRotate() ? "&aTrue" : "&cFalse") + "&7.");
        }

    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
