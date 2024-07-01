package org.mammothplugins.ssm.command;

import org.bukkit.entity.Player;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.ssm.kits.Kits;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

final class GameSelectKitCommand extends GameSubCommand {

    GameSelectKitCommand() {
        super("kit/k");

        this.setDescription("Select a Kit");
        this.setPermission("ssm.cmd.player.kit");
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
                if (pl.getName().equals(this.args[1]) && getSender().hasPermission("ssm.cmd.admin.forcekit")) {
                    cache = PlayerCache.from(pl);
                    player = pl;
                }
        }
        this.checkBoolean(player != null, "Player " + args[1] + " does not exist.");
        this.checkBoolean(cache.hasGame(), "Can only change kits when the player is in the game Lobby!");
        this.checkBoolean(cache.getCurrentGame().isLobby(), "Can only change kits when the player is in the game Lobby!");

        if ("random".equals(this.args[0]))
            if (player.hasPermission("ssm.kits.random")) {
                cache.setRandomKit(true);
                Common.tell(getSender(), "You forced " + player.getName() + " to have a random kit.");
                Common.tell(player, "Your kit has changed to a random kit");
                CompSound.CAT_MEOW.play(player, 5.0F, 0.3F);
                if (getSender() instanceof Player)
                    CompSound.CAT_MEOW.play(getPlayer(), 5.0F, 0.3F);
            } else {
                Common.tell(getSender(), "The player " + player.getName() + " does not have permission to use Random Kits.");
                if (getSender() instanceof Player)
                    CompSound.VILLAGER_NO.play(getPlayer());
            }
        for (Kits kit : Kits.getKits())
            if (kit.getName().equals(this.args[0])) {
                if (player.hasPermission("ssm.kits." + kit.getName())) {
                    cache.setCurrentKit(kit);
                    Common.tell(getSender(), "You forced " + player.getName() + " to have the kit " + kit.getName() + ".");
                    Common.tell(player, "Your kit has changed to the kit " + kit.getName() + ".");
                    CompSound.CAT_MEOW.play(player, 5.0F, 0.3F);
                    if (getSender() instanceof Player)
                        CompSound.CAT_MEOW.play(getPlayer(), 5.0F, 0.3F);
                } else {
                    Common.tell(getSender(), "The player " + player.getName() + " does not have permission to use the kit " + kit.getName() + ".");
                    if (getSender() instanceof Player)
                        CompSound.VILLAGER_NO.play(getPlayer());
                }
            }
    }

    @Override
    protected List<String> tabComplete() {
        return (this.args.length == 1 ? this.completeLastWord(Kits.getKitsNames(), "random") :
                (this.args.length == 2 ? this.completeLastWord(Remain.getOnlinePlayers()) : NO_COMPLETE));
    }
}