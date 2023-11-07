package org.mammothplugins.baconBrawl.command;

import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

final class GameJoinCommand extends GameSubCommand {

    GameJoinCommand() {
        super("join/j");

        this.setDescription("Joins a game.");
        this.setUsage("[name]");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        if (this.args.length > 0 && "all".equals(this.args[0])) {
            Game firstGame = Game.getGames().get(0);
            this.checkBoolean(firstGame.isStopped(), "Can only use this command for stopped games.");

            for (Player online : Remain.getOnlinePlayers())
                firstGame.joinPlayer(online, GameJoinMode.PLAYING);

            firstGame.start();

            return;
        }

        Game game = this.findGameFromLocationOrFirstArg();

        game.joinPlayer(this.getPlayer(), GameJoinMode.PLAYING);
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames(), "all") : NO_COMPLETE;
    }
}
