package org.mammothplugins.ssm.command;

import org.bukkit.entity.Player;
import org.mammothplugins.ssm.model.Game;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

final class GameJoinCommand extends GameSubCommand {

    GameJoinCommand() {
        super("join/j");

        this.setDescription("Joins a game.");
        this.setUsage("[name]");
        setPermission("ssm.cmd.player.join");
    }

    @Override
    protected void onCommand() {
        if (this.args.length > 0) {
            if ("all".equals(this.args[0])) {
                Game firstGame = Game.getGames().get(0);
                this.checkBoolean(firstGame.isStopped(), "Can only use this command for stopped games.");

                for (Player online : Remain.getOnlinePlayers())
                    firstGame.joinPlayer(online, GameJoinMode.PLAYING);

                return;
            }
            for (Player player : Remain.getOnlinePlayers())
                if (player.getName().equals(this.args[1]) && getSender().hasPermission("ssm.cmd.admin.forcejoin")) {
                    Game game = this.findGameFromLocationOrFirstArg();

                    game.joinPlayer(player, GameJoinMode.PLAYING);
                    Common.tell(getSender(), "You forced " + player.getName() + " to join " + game.getName() + ".");
                    if (getSender() instanceof Player)
                        CompSound.CAT_MEOW.play(getPlayer(), 5.0F, 0.3F);

                    return;
                } else {
                    if (getSender() instanceof Player)
                        CompSound.VILLAGER_NO.play(getPlayer());
                }
        }

        this.checkConsole();
        Game game = this.findGameFromLocationOrFirstArg();

        game.joinPlayer(this.getPlayer(), GameJoinMode.PLAYING);
    }

    @Override
    protected List<String> tabComplete() {
        return (this.args.length == 1 ? this.completeLastWord(Game.getGameNames(), "all") : (this.args.length == 2 ? this.completeLastWord(Remain.getOnlinePlayers()) : NO_COMPLETE));
    }
}
