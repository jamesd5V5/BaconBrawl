package org.mammothplugins.ssm.design;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.mammothplugins.ssm.model.Game;
import org.mineacademy.fo.Common;

import java.util.HashMap;
import java.util.UUID;

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

    public static void deathMessage(Game game, PlayerDeathEvent event, HashMap<UUID, UUID> lastHit) {
        Player victim = event.getEntity();
        EntityDamageEvent.DamageCause damageCause = victim.getLastDamageCause().getCause();

        Common.broadcast("test: " + lastHit.get(victim.getUniqueId()).toString());
        if (lastHit.get(victim.getUniqueId()) == null) {
            for (Player player : game.getBukkitPlayersInAllModes()) {
                String name = checkUUID(player, victim) ? "You" : victim.getName();
                if (damageCause == EntityDamageEvent.DamageCause.LAVA || damageCause == EntityDamageEvent.DamageCause.FIRE)
                    Common.tell(player, "&7" + name + " burned to death.");
                else
                    Common.tell(player, "&7" + name + " fell into the void.");
            }
        } else {
            Player damager = Bukkit.getPlayer(lastHit.get(victim.getUniqueId()));
            for (Player player : game.getBukkitPlayersInAllModes()) {
                String name = checkUUID(player, victim) ? "&7you" : "&e" + victim.getName();
                if (damageCause == EntityDamageEvent.DamageCause.VOID)
                    Common.tell(player, "&e" + damager.getName() + " &7knocked &e" + name + " &7into the void.");
                else if (!damageCause.name().equals("CUSTOM"))
                    Common.tell(player, "&e" + damager.getName() + " &7killed " + name + " &7with &a" + deathCauseInterpretation(damageCause) + "&7.");
                else
                    Common.tell(player, "&e" + damager.getName() + " &7killed " + name + "&7.");
            }
            lastHit.remove(victim.getUniqueId());
        }
    }

    private static String deathCauseInterpretation(EntityDamageEvent.DamageCause damageCause) {
        String deathReason = damageCause.name();
        if (deathReason.equals("ENTITY_ATTACK"))
            deathReason = "an attack";
        if (deathReason.equals("ENTITY_EXPLOSION"))
            deathReason = "an explosion";
        return deathReason;
    }

    public static void clearChat(Player player) {
        Common.tellNoPrefix(player, " ", " ", " ", " ", " ");
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
