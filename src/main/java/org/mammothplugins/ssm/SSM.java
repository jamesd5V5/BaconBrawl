package org.mammothplugins.ssm;

import org.mammothplugins.ssm.apis.PlaceHolderApi;
import org.mammothplugins.ssm.bungee.GameBungeeListener;
import org.mammothplugins.ssm.model.Game;
import org.mammothplugins.ssm.model.GameStopReason;
import org.mammothplugins.ssm.task.EscapeTask;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.model.Variable;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.plugin.SimplePlugin;

public final class SSM extends SimplePlugin {

    @Override
    protected void onPluginStart() {
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

        Variable.loadVariables();

        // Register variables - PlaceholderAPI compatible
        Variables.addExpansion(PlaceHolderApi.getInstance());

        Game.loadGames();

        GameBungeeListener.scheduleTask();
    }

    /* ------------------------------------------------------------------------------- */
    /* Static */
    /* ------------------------------------------------------------------------------- */

    public static SSM getInstance() {
        return (SSM) SimplePlugin.getInstance();
    }
}
