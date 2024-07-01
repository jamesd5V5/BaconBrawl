package org.mammothplugins.ssm.model.ssm.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mammothplugins.ssm.SSM;
import org.mammothplugins.ssm.model.ssm.kits.nms.NmsDisguise;
import org.mammothplugins.ssm.model.ssm.kits.powers.Power;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MagmaCube extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private SlimeWatcher slimeWatcher;
    private int currentSize = 1;

    public MagmaCube() {
        setName("MagmaCube");
        setLore(new String[]{"Just a baby.\n" + "O now he's a BIG baby!"});
        setChatColor(ChatColor.DARK_RED);
        setCompMaterial(CompMaterial.FIRE_CHARGE);
        setDamage(5);
        setRegen(0.35);
        setKnockback(175);
    }

    @Override
    public void usePower(Player player, String powerName) {
        super.usePower(player, powerName);

        if (powerName == "Magma Blast")
            kitPowers.get(player.getUniqueId()).get(0).activatePower();
        if (powerName == "Flame Dash")
            kitPowers.get(player.getUniqueId()).get(1).activatePower();
    }


    @Override
    public void applyAttributes(Player player) {
        super.applyAttributes(player);

        applyArmor(player);
        givePowers(player);

        Disguise disguise = NmsDisguise.setDisguise(player, DisguiseType.MAGMA_CUBE);

        this.slimeWatcher = (SlimeWatcher) disguise.getWatcher();
        slimeWatcher.setSize(1);
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        MagmaBlastPower magmaBlastPower = new MagmaBlastPower(player);
        powers.add(magmaBlastPower);
        addPower(magmaBlastPower, player);
        magmaBlastPower.givePowerItem();

        FlameDashPower flameDashPower = new FlameDashPower(player);
        powers.add(flameDashPower);
        addPower(flameDashPower, player);
        flameDashPower.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }

    private void applyArmor(Player player) {
        ItemStack chestplate = ItemCreator.of(CompMaterial.CHAINMAIL_CHESTPLATE, "&a&l&").make();
        ItemStack pants = ItemCreator.of(CompMaterial.CHAINMAIL_LEGGINGS, "&a&lMeg").make();
        ItemStack boots = ItemCreator.of(CompMaterial.CHAINMAIL_BOOTS, "&a&lCreamly").make();

        player.getEquipment().setChestplate(chestplate);
        player.getEquipment().setLeggings(pants);
        player.getEquipment().setBoots(boots);
    }

    @Override
    public void setOnFire(Player player, int ticks) {
        //Magmas dont catch on fire
    }

    public void onKill() {
        if (currentSize >= 4)
            return;
        currentSize++;
        slimeWatcher.setSize(currentSize);
        setDamage(getDamage() + 1);
    }

    @Override
    public void onDeath(Player player) {
        super.onDeath(player);
        currentSize = 1;
        setDamage(5);
        if (slimeWatcher != null) {
            slimeWatcher.setSize(1);
        }
    }

    public class MagmaBlastPower extends Power {

        private Player player = getPlayer();

        public MagmaBlastPower(Player player) {
            super("Magma Blast",
                    ItemCreator.of(CompMaterial.IRON_AXE, "&f&lMagma Blast").make(), player, 3, 1);
        }

        @Override
        public void activatePower() {
            Vector playerDirection = player.getEyeLocation().getDirection();
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.setVelocity(playerDirection.multiply(1));
            CompMetadata.setTempMetadata(fireball, "MagmaBlast");
            player.setVelocity(playerDirection.multiply(-0.75));
            CompSound.GHAST_FIREBALL.play(fireball.getLocation(), 0.5f, 0.8f);
        }

        @Override
        public void postActivatedProjectile(LivingEntity victim, Projectile projectile) {
            Location projLocation = projectile.getLocation();
            for (Entity entity : projectile.getNearbyEntities(5, 5, 5))
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (!livingEntity.getUniqueId().equals(player.getUniqueId()))
                        livingEntity.damage(6);
                }
            CompSound.EXPLODE.play(projLocation, 0.5f, 0.8f);
            CompSound.ENTITY_ARROW_HIT_PLAYER.play(player.getLocation(), 0.5f, 1f);
            victim.setVelocity(projectile.getVelocity().add(new Vector(0, 1.75, 0)));
        }
    }

    private class FlameDashPower extends Power {

        private Player player = getPlayer();

        public FlameDashPower(Player player) {
            super("Flame Dash",
                    ItemCreator.of(CompMaterial.IRON_SHOVEL, "&f&lFlame Dash").make(), player, 6, 1);
        }

        @Override
        public void activatePower() {
            Vector playerDirection = player.getEyeLocation().getDirection().setY(0);

            final boolean[] hasLaunched = {false};
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead() || hasLaunched[0]) {
                        cancel();
                        return;
                    }
                    CompSound.FIZZ.play(player.getLocation(), 0.1f, 0.1f);
                    player.setVelocity(playerDirection.multiply(1));
                    player.setVelocity(player.getVelocity().setY(0));

                    for (int i = 0; i < 10; i++)
                        CompParticle.FLAME.spawn(RandomUtil.nextLocation(player.getLocation(), 1, true));

                    for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
                        if (entity instanceof LivingEntity) {
                            Vector launchDirection = entity.getLocation().toVector().add(player.getLocation().toVector()).normalize().multiply(0.3);
                            launchDirection.setY(0.5);
                            entity.setVelocity(launchDirection);
                            ((LivingEntity) entity).damage(1);
                            CompSound.ENTITY_ARROW_HIT_PLAYER.play(player, 0.5f, 1f);

                        }
                    }
                }
            }.runTaskTimer(SSM.getInstance(), 0, 0L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead()) {
                        cancel();
                        return;
                    }

                    for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
                        if (entity instanceof LivingEntity) {
                            Vector launchDirection = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.9);
                            launchDirection.setY(1);
                            entity.setVelocity(launchDirection);
                            ((LivingEntity) entity).damage(2);

                        }
                    }

                    CompSound.EXPLODE.play(player.getLocation(), 0.25f, 0.25f);
                    for (int i = 0; i < 15; i++) {
                        CompParticle.FLAME.spawn(RandomUtil.nextLocation(player.getLocation(), 2, true));
                        CompParticle.EXPLOSION_NORMAL.spawn(RandomUtil.nextLocation(player.getLocation(), 2, true));
                    }
                    hasLaunched[0] = true;


                }
            }.runTaskLater(SSM.getInstance(), 20L);
        }
    }
}
