package org.mammothplugins.baconBrawl.command;

import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

final class GameSpectateCommand extends GameSubCommand {

    GameSpectateCommand() {
        super("spectate/sp");

        this.setDescription("Spectates an ongoing game.");
        this.setUsage("[name]");
        this.setPermission("baconbrawl.cmd.player.spectate");
    }

    @Override
    protected void onCommand() {
        PlayerCache cache = null;
        Player player = null;
        if (getSender() instanceof Player && args.length == 0) {
            cache = PlayerCache.from(getPlayer());
            player = getPlayer();
        } else {
            for (Player pl : Remain.getOnlinePlayers())
                if (pl.getName().equals(this.args[1]) && getSender().hasPermission("baconbrawl.cmd.admin.forcespectate")) {
                    cache = PlayerCache.from(pl);
                    player = pl;
                }
        }
        this.checkBoolean(player != null, "Player " + args[1] + " does not exist.");
        this.checkBoolean(cache.hasGame(), "Can only spectate when the game is in play!");
        this.checkBoolean(cache.getCurrentGame().isPlayed(), "You can only spectate games that are played.");
        this.checkBoolean(cache.getCurrentGame().findPlayer(getPlayer()) == null, "You cannot spectate a game you are playing. Type '/" + getLabel() + " leave' to exit.");
        Game game = cache.getCurrentGame();
        game.joinPlayer(this.getPlayer(), GameJoinMode.SPECTATING);
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : (this.args.length == 2 ? this.completeLastWord(Remain.getOnlinePlayers()) : NO_COMPLETE);
    }
}
