package org.mammothplugins.ssm.command;

import org.bukkit.entity.Player;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.Game;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mammothplugins.ssm.model.GameLeaveReason;
import org.mammothplugins.ssm.settings.Settings;
import org.mineacademy.fo.Common;

import java.util.List;

final class GameEditCommand extends GameSubCommand {

    GameEditCommand() {
        super("edit/e");

        this.setDescription("Joins a game for editing.");
        this.setUsage("[name]");
        this.setMinArguments(0);
        this.setPermission("ssm.cmd.admin.edit");
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

            if (Settings.AutoMode.ENABLED)
                game.joinPlayer(player, GameJoinMode.EDITING);

        } else
            game.joinPlayer(player, GameJoinMode.EDITING);
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}
