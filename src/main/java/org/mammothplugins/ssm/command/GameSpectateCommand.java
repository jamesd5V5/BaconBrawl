package org.mammothplugins.ssm.command;

import org.bukkit.entity.Player;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.Game;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

final class GameSpectateCommand extends GameSubCommand {

    GameSpectateCommand() {
        super("spectate/sp");

        this.setDescription("Spectates an ongoing game.");
        this.setUsage("[name]");
        this.setPermission("ssm.cmd.player.spectate");
    }

    @Override
    protected void onCommand() {
//        Game game = this.findGameFromLocationOrFirstArg();
//        PlayerCache cache = null;
//        Player player = null;
//        if (getSender() instanceof Player && args.length == 0) {
//            cache = PlayerCache.from(getPlayer());
//            player = getPlayer();
//        } else {
//            if (args.length > 1)
//                for (Player pl : Remain.getOnlinePlayers())
//                    if (pl.getName().equals(this.args[1]) && getSender().hasPermission("ssm.cmd.admin.forcespectate")) {
//                        cache = PlayerCache.from(pl);
//                        player = pl;
//                    }
//        }
//        this.checkBoolean(player != null, "Player " + args[1] + " does not exist.");
//        this.checkBoolean(game.isPlayed(), "Can only spectate when the game is in play!");
//        //this.checkBoolean(cache.getCurrentGame().isPlayed(), "You can only spectate games that are played.");
//        this.checkBoolean(cache.getCurrentGame().findPlayer(getPlayer()) == null, "You cannot spectate a game you are playing. Type '/" + getLabel() + " leave' to exit.");
//        game.joinPlayer(this.getPlayer(), GameJoinMode.SPECTATING);
        Game game = this.findGameFromLocationOrFirstArg();
        PlayerCache cache = null;
        Player targetPlayer = null;
        if (getSender() instanceof Player) {
            cache = PlayerCache.from(getPlayer());
            targetPlayer = getPlayer();
        }
        if (args.length > 1 && getSender().hasPermission("ssm.cmd.admin.forcespectate")) {

            for (Player pl : Remain.getOnlinePlayers())
                if (pl.getName().equals(this.args[1]) && getSender().hasPermission("ssm.cmd.admin.forcespectate"))
                    targetPlayer = pl;

            this.checkBoolean(targetPlayer != null, "Player " + args[1] + " does not exist.");

            cache = PlayerCache.from(targetPlayer);
            sender = targetPlayer;
        }

        this.checkBoolean(game != null, "Game not found.");
        this.checkBoolean(game.isPlayed(), "Cannot spectate when the game is not in play.");
        this.checkBoolean(cache != null, "Player does not exist.");
        this.checkBoolean(cache.getCurrentGame() != game, "You cannot spectate a game you are playing. Type '/" + getLabel() + " leave' to exit.");

        game.joinPlayer(targetPlayer, GameJoinMode.SPECTATING);
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : (this.args.length == 2 ? this.completeLastWord(Remain.getOnlinePlayers()) : NO_COMPLETE);
    }
}
