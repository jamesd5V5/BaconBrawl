package org.mammothplugins.baconBrawl.command;

import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mammothplugins.baconBrawl.model.GameLeaveReason;
import org.mammothplugins.baconBrawl.settings.Settings;
import org.mammothplugins.baconBrawl.tool.MapCreatorTool;
import org.mineacademy.fo.Common;

import java.util.List;

final class GameEditCommand extends GameSubCommand {

    GameEditCommand() {
        super("edit/e");

        this.setDescription("Joins a game for editing.");
        this.setUsage("[name]");
        this.setMinArguments(0);
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        Game game;
        final Player player = this.getPlayer();

        if (args.length == 0) {
            game = PlayerCache.from(player).getCurrentGame();

            if (game == null)
                game = Game.findByLocation(getPlayer().getLocation());

            this.checkNotNull(game, "Unable to locate a game. Type a game name. Available: " + Common.join(Game.getGameNames()));

        } else {
            final String gameName = this.joinArgs(0);
            this.checkGameExists(gameName);

            game = Game.findByName(gameName);
        }

        if (game.isJoined(player)) {
            getCache().setPlayerTag("SwitchToEditing", true);
            game.leavePlayer(player, GameLeaveReason.COMMAND);

            if (Settings.AutoMode.ENABLED) {
                game.joinPlayer(player, GameJoinMode.EDITING);
                MapCreatorTool.getInstance().give(player, 4);
            }

        } else {
            game.joinPlayer(player, GameJoinMode.EDITING);
            MapCreatorTool.getInstance().give(player, 4);
        }
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}
