package org.mammothplugins.baconBrawl;

import org.mammothplugins.baconBrawl.bungee.GameBungeeListener;
import org.mammothplugins.baconBrawl.command.NmsCommand;
import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameStopReason;
import org.mammothplugins.baconBrawl.task.EscapeTask;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;

public final class BaconBrawl extends SimplePlugin {

    @Override
    protected void onPluginStart() {

        registerCommand(new NmsCommand());
    }

    /**
     * Automatically perform login when the plugin starts and each time it is reloaded.
     */
    @Override
    protected void onReloadablesStart() {

        Common.runTimer(1 * 20, new EscapeTask());

        for (Game game : Game.getGames())
            if (!game.isStopped())
                game.stop(GameStopReason.RELOAD);

        Game.loadGames();

        GameBungeeListener.scheduleTask();
    }

    /* ------------------------------------------------------------------------------- */
    /* Static */
    /* ------------------------------------------------------------------------------- */

    public static BaconBrawl getInstance() {
        return (BaconBrawl) SimplePlugin.getInstance();
    }
}
