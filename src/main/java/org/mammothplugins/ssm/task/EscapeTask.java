package org.mammothplugins.ssm.task;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.Game;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mammothplugins.ssm.model.GameLeaveReason;
import org.mammothplugins.ssm.model.ssm.SSMCore;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.Remain;

public final class EscapeTask extends BukkitRunnable {

    @Override
    public void run() {

        for (Player online : Remain.getOnlinePlayers()) {
            Location location = online.getLocation();
            int minHeight = MinecraftVersion.atLeast(MinecraftVersion.V.v1_16) ? location.getWorld().getMinHeight() : 0;

            if (online.isDead() || location.getY() <= minHeight || CompMetadata.hasTempMetadata(online, Game.TAG_TELEPORTING))
                continue;

            PlayerCache cache = PlayerCache.from(online);

            if (cache.hasGame() && !cache.isLeaving() && !cache.isJoining() && cache.getCurrentGameMode() != GameJoinMode.EDITING) {
                Game game = cache.getCurrentGame();

                if (!game.isStarting() && !game.isStopping()) {
                    //For most games, we need this, but not needed for BB
                    if (!game.getRegion().isWithin(location)) {
                        if (game instanceof SSMCore) {
                            if (game.isLobby()) {
                                cache.toPlayer().teleport(game.getLobbyLocation());
                            } else if (!game.isStopped())
                                cache.toPlayer().setHealth(0);//kills player when they fall in the void

                        } else {
                            game.leavePlayer(online, GameLeaveReason.ESCAPED);
                            Messenger.warn(online, "You've escaped the game and were kicked from playing!");
                        }
                    }
                }
            }
        }
    }
}
