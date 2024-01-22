package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.baconbrawl.BaconBrawlCore;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.remain.CompSound;

import java.util.List;

final class GamePorkalypseModeCommand extends GameSubCommand {

    GamePorkalypseModeCommand() {
        super("porkalypsemode/pm");

        this.setDescription("Set whether the game should have PorkalypseMode.");
        this.setPermission("baconbrawl.cmd.admin.porkalypsemode");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        BaconBrawlCore game = (BaconBrawlCore) PlayerCache.from(getPlayer()).getCurrentGame();

        if (game == null)
            Messenger.error(getPlayer(), "You must be editing a game to use this command!");
        else {
            if (game.isCanHavePorkalypseMode())
                game.setcanHavePorkalypseMode(false);
            else
                game.setcanHavePorkalypseMode(true);
            CompSound.CAT_MEOW.play(getPlayer(), 5.0F, 0.3F);
            Common.tell(getPlayer(), "&7PorkalypseMode has been set to " + (game.isCanHavePorkalypseMode() ? "&aTrue" : "&cFalse") + "&7.")
            ;
        }

    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
