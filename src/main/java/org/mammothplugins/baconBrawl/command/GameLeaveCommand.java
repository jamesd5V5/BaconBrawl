package org.mammothplugins.baconBrawl.command;

import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameLeaveReason;
import org.mammothplugins.baconBrawl.model.GameState;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

final class GameLeaveCommand extends GameSubCommand {

    GameLeaveCommand() {
        super("leave/l");

        this.setDescription("Leaves the game you are currently playing.");
        setPermission("baconbrawl.cmd.player.leave");
    }

    @Override
    protected void onCommand() {
        if (this.args.length > 0)
            for (Player player : Remain.getOnlinePlayers())
                if (player.getName().equals(this.args[0]) && getSender().hasPermission("baconbrawl.cmd.admin.forceleave")) {
                    final PlayerCache cache = PlayerCache.from(player);
                    if (cache.hasGame())
                        this.checkBoolean(cache.getCurrentGame().getState() != GameState.PREPLAYED, "Cannot leave the game until it starts!");
                    this.checkBoolean(cache.hasGame(), player.getName() + " are not playing any game right now.");

                    Common.tell(getSender(), "You forced " + player.getName() + " to leave " + cache.getCurrentGame().getName() + ".");
                    if (getSender() instanceof Player)
                        CompSound.CAT_MEOW.play(getPlayer(), 5.0F, 0.3F);
                    cache.getCurrentGame().leavePlayer(player, GameLeaveReason.COMMAND);
                    return;
                } else {
                    if (getSender() instanceof Player)
                        CompSound.VILLAGER_NO.play(getPlayer());
                }

        this.checkConsole();

        final PlayerCache cache = PlayerCache.from(this.getPlayer());
        if (cache.hasGame())
            this.checkBoolean(cache.getCurrentGame().getState() != GameState.PREPLAYED, "Cannot leave the game until it starts!");
        this.checkBoolean(cache.hasGame(), "You are not playing any game right now.");

        cache.getCurrentGame().leavePlayer(getPlayer(), GameLeaveReason.COMMAND);
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames(), Remain.getOnlinePlayers()) : NO_COMPLETE;
    }
}
