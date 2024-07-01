package org.mammothplugins.ssm.model.ssm.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.BlazeWatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.SSM;
import org.mammothplugins.ssm.model.ssm.kits.nms.NmsDisguise;
import org.mammothplugins.ssm.model.ssm.kits.powers.Power;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Blaze extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private BlazeWatcher blazeWatcher;

    public Blaze() {
        setName("Blaze");
        setLore(new String[]{"BURN Baby BURN!\n" + "I got that rod in me"});
        setChatColor(ChatColor.YELLOW);
        setCompMaterial(CompMaterial.BLAZE_POWDER);
        setDamage(6);
        setRegen(0.15);
        setKnockback(150);
    }

    @Override
    public void usePower(Player player, String powerName) {
        super.usePower(player, powerName);

        if (powerName == "Inferno")
            kitPowers.get(player.getUniqueId()).get(0).activatePower();
        if (powerName == "Phoenix")
            kitPowers.get(player.getUniqueId()).get(1).activatePower();
    }


    @Override
    public void applyAttributes(Player player) {
        super.applyAttributes(player);

        applyArmor(player);
        givePowers(player);

        Disguise disguise = NmsDisguise.setDisguise(player, DisguiseType.BLAZE);
        this.blazeWatcher = (BlazeWatcher) disguise.getWatcher();
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        InfernoPower infernoPower = new InfernoPower(player);
        powers.add(infernoPower);
        addPower(infernoPower, player);
        infernoPower.givePowerItem();

        FireflyPower fireflyPower = new FireflyPower(player);
        powers.add(fireflyPower);
        addPower(fireflyPower, player);
        fireflyPower.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }

    private boolean isHitInLaunch = false;

    public boolean isHitInLaunch(Player player) {
        return isHitInLaunch;
    }

    public void setIsHitInLaunch(boolean isHitInLaunch) {
        this.isHitInLaunch = isHitInLaunch;
    }

    private boolean isLaunching = false;

    public boolean isLaunching(Player player) {
        return isLaunching;
    }

    public void setIsLaunching(boolean isLaunching) {
        this.isLaunching = isLaunching;
    }

    private void applyArmor(Player player) {
        ItemStack helmet = ItemCreator.of(CompMaterial.CHAINMAIL_HELMET, "&c&lIts getting").make();
        ItemStack chestplate = ItemCreator.of(CompMaterial.CHAINMAIL_CHESTPLATE, "&6&lHOT in here").make();
        ItemStack pants = ItemCreator.of(CompMaterial.CHAINMAIL_LEGGINGS, "&c&lSo... take off").make();
        ItemStack boots = ItemCreator.of(CompMaterial.CHAINMAIL_BOOTS, "&6&lALL ur Clothes").make();

        player.getEquipment().setHelmet(helmet);
        player.getEquipment().setChestplate(chestplate);
        player.getEquipment().setLeggings(pants);
        player.getEquipment().setBoots(boots);
    }

    public class InfernoPower extends Power {

        private Player player = getPlayer();
        private boolean finishedInferno = false;

        public InfernoPower(Player player) {
            super("Inferno", ItemCreator.of(CompMaterial.IRON_SWORD,
                    "&f&lInferno").make(), player, 4, 1, true);
        }

        @Override
        public void activatePower() {
            final boolean[] stop = {false};
            PlayerCache cache = PlayerCache.from(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead() || stop[0]) {
                        finishedInferno = true;
                        cancel();
                        return;
                    }
                    if (cache.isCurrentlyBlocking()) {
                        Snowball proj = player.launchProjectile(Snowball.class);
                        proj.setVelocity(proj.getVelocity().multiply(0.5));
                        CompSound.ENTITY_BLAZE_SHOOT.play(player.getLocation(), 0.2f, 1f);
                        ItemStack blazePowder = ItemCreator.of(CompMaterial.BLAZE_POWDER, "InfernoPowder").make();
                        proj.setItem(blazePowder);
                        CompMetadata.setTempMetadata(proj, "InfernoPowder");
                    } else {
                        Common.tell(player, "Stopped Inferno");
                        finishedInferno = true;
                        cancel();
                        return;
                    }
                }
            }.runTaskTimer(SSM.getInstance(), 0L, 3L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    stop[0] = true;
                }
            }.runTaskLater(SSM.getInstance(), 20L);
        }

        @Override
        public void postActivatedProjectile(LivingEntity victim, Projectile projectile) {
            victim.damage(3);
            victim.setFireTicks(70);
            if (victim instanceof Player)
                setOnFire(player, 70);
        }

        @Override
        public boolean canStartCooldown() {
            return finishedInferno;
        }
    }

    private class FireflyPower extends Power {

        private Player player = getPlayer();

        public FireflyPower(Player player) {
            super("Phoenix", ItemCreator.of(CompMaterial.IRON_AXE,
                    "&f&lPhoenix").make(), player, 6, 4);
        }

        @Override
        public void activatePower() {
            Vector vector = new Vector(0, 0, 0);
            player.setVelocity(vector);
            final boolean[] hasLaunched = {false};
            final boolean[] stop = {false};

            double playerHealth = player.getHealth();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead() || hasLaunched[0] || stop[0] || player.getHealth() != playerHealth || isHitInLaunch(player)) {
                        setIsLaunching(false);
                        cancel();
                        return;
                    }
                    player.setVelocity(vector);
                    CompSound.FIREWORK_BLAST.play(player.getLocation(), 0.25f, 1f);
                    setIsLaunching(true);
                    for (int i = 0; i < 10; i++) {
                        CompParticle.SMOKE_NORMAL.spawn(RandomUtil.nextLocation(player.getLocation(), 2, true));
                        CompParticle.FLAME.spawn(RandomUtil.nextLocation(player.getLocation(), 1, true));
                    }
                }
            }.runTaskTimer(SSM.getInstance(), 0, 0L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead() || stop[0] || isHitInLaunch(player)) {
                        setIsHitInLaunch(false);
                        cancel();
                        return;
                    }

                    for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
                        if (entity instanceof LivingEntity) {
                            Vector launchDirection = player.getVelocity().multiply(0.2).setY(0);
                            launchDirection.add(new Vector(0, 0.5, 0));
                            entity.setVelocity(launchDirection);
                            ((LivingEntity) entity).damage(4);

                        }
                    }

                    CompSound.ITEM_FIRECHARGE_USE.play(player.getLocation(), 0.25f, 1f);
                    player.setVelocity(player.getEyeLocation().getDirection().multiply(1));
                    hasLaunched[0] = true;
                }
            }.runTaskTimer(SSM.getInstance(), 30l, 0L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    stop[0] = true;
                }
            }.runTaskLater(SSM.getInstance(), 60L);
        }
    }
}
