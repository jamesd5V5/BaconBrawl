package org.mammothplugins.baconBrawl.design;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.Kits;
import org.mineacademy.fo.Common;

public class PlayerUIDesigns {

    public static String getLaunchBar(double currentTime, int maxTime) {
        double ratioFactor = (double) maxTime / 50.0;

        // Convert max and current values using the ratio factor
        int convertedMax = (int) Math.round(maxTime / ratioFactor);
        int convertedCurrent = (int) Math.round(currentTime / ratioFactor);

        String box = "\u2588";
        String line = "|";
        String progress1 = "&c" + line.repeat(convertedCurrent);
        String progress2 = "&a" + line.repeat(convertedMax - convertedCurrent);

        return progress2 + progress1;
    }

    public static void deathMessage(Game game, PlayerDeathEvent event) {
        Player victim = event.getEntity();
        //event.get
        EntityDamageEvent.DamageCause damageCause = victim.getLastDamageCause().getCause();

        Player attacker = victim.getKiller();
        if (attacker == null) {
            for (Player player : game.getBukkitPlayersInAllModes()) {
                String name = checkUUID(player, victim) ? "You" : victim.getName();
                if (damageCause == EntityDamageEvent.DamageCause.VOID)
                    Common.tell(player, "&7" + name + " fell into the void.");
                if (damageCause == EntityDamageEvent.DamageCause.LAVA || damageCause == EntityDamageEvent.DamageCause.FIRE)
                    Common.tell(player, "&7" + name + " burned to death.");
            }
        } else {

            PlayerCache victimCache = PlayerCache.from(victim);
            PlayerCache attackerCache = PlayerCache.from(victim);

            if (attackerCache == null || attackerCache.getCurrentGameMode() == GameJoinMode.SPECTATING ||
                    victimCache == null || victimCache.getCurrentGameMode() == GameJoinMode.SPECTATING)
                return;
            if (attackerCache != null && attackerCache.getCurrentGameMode() == GameJoinMode.PLAYING &&
                    victimCache != null || victimCache.getCurrentGameMode() == GameJoinMode.PLAYING) {
                Kits victimsKit = victimCache.getCurrentKit();
                Kits attackersKit = attackerCache.getCurrentKit();

                for (Player player : game.getBukkitPlayersInAllModes()) {
                    String name = checkUUID(player, victim) ? "&7you" : "&e" + victim.getName();
                    Common.tell(player, "&e" + attacker.getName() + " &7killed " + name + " &7with &a" + damageCause.name());
                }
            }
        }
    }

    private static boolean checkUUID(Player player, Player player2) {
        if (player.getUniqueId().equals(player2.getUniqueId()))
            return true;
        return false;
    }

    private static String test() {
        return ("\u2591 __ \u2592 __ \u2593 __ \u2588 __ \u261B __ \u265A __ \u265B __ \u265C __ \u265D __ \u265E __ \u265F __ \u261A __ \u2588 __ \u2593 __ \u2592  __ \u2591");
    }
}
