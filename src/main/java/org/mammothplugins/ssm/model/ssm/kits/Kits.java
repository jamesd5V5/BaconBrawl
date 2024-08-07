package org.mammothplugins.ssm.model.ssm.kits;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mammothplugins.ssm.model.ssm.kits.powers.Power;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Kits {
    private static boolean hadDelayedSetup = false;
    private static List<Kits> kits = new ArrayList<>();

    private String name;
    private String[] lore;
    private ChatColor chatColor;
    private CompMaterial compMaterial;

    private double damage;
    private double regen;
    private double knockback;


    private HashMap<UUID, ArrayList<Power>> powers = new HashMap<>();

    public Kits() {
    }

    public Kits(String typeName) {

    }

    static {
        kits.add(new Creeper());
        kits.add(new MagmaCube());
        kits.add(new Blaze());
        kits.add(new WitherSkeleton());
        kits.add(new Enderman());

        Common.runLater(4, () -> {
            hadDelayedSetup = true;
        });
    }

    public void usePower(Player player, Power power) {
        usePower(player, power.getName());
    }

    public void usePower(Player player, String powerName) {
    }

    public void setOnFire(Player player, int ticks) {
    }


    public void applyAttributes(Player player) {
        player.setAllowFlight(true);
        player.getInventory().setItem(8, ItemCreator.of(CompMaterial.COMPASS, "&f&lTracker").make());
    }

    public void onDeath(Player player) {
    }

    public void clearItems(Player player) {
        player.getInventory().clear();
        getPowers(player).clear();
    }

    public static List<Kits> getKits() {
        return kits;
    }

    public void wipeAllPowers() {
        powers.clear();
    }

    public ArrayList<Power> getPowers(Player player) {
        if (powers.get(player.getUniqueId()) == null)
            return new ArrayList<Power>();
        return powers.get(player.getUniqueId());
    }

    public void addPower(Power power, Player player) {
        if (getPowers(player) == null) {
            ArrayList powerList = new ArrayList();
            powerList.add(power);
            powers.put(player.getUniqueId(), powerList);
        } else {
            ArrayList powerList = getPowers(player);
            powerList.add(power);
            powers.put(player.getUniqueId(), powerList);
        }
    }

    public static boolean hadDelayedSetup() {
        return hadDelayedSetup;
    }

    public static List<String> getKitsNames() {
        ArrayList<String> list = new ArrayList<>();
        for (Kits kit : kits)
            list.add(kit.getName());
        return list;
    }

    public static Kits getKit(String name) {
        for (Kits kit : kits) {
            if (kit.getName().equals(name))
                return kit;
        }
        return null;
    }
}
