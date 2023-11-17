package org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.mammothplugins.baconBrawl.BaconBrawl;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;

@Getter
public class Power {

    private String name;
    private ItemStack itemStack;
    private Player player;
    private int cooldown;
    private int silentSeconds;
    private boolean usesBlocking;

    private boolean resetPower = false;
    private boolean canStartCooldown = false;

    private boolean isCoolingDown = false;
    private double timeLeftToCooldown = 0.0;

    public Power(String name, ItemStack itemStack, Player player) {
        this(name, itemStack, player, 3);
    }

    public Power(String name, ItemStack itemStack, Player player, int cooldown) {
        this(name, itemStack, player, cooldown, 1);
    }

    public Power(String name, ItemStack itemStack, Player player, int cooldown, int quietCooldown) {
        this(name, itemStack, player, cooldown, quietCooldown, false);
    }

    public Power(String name, ItemStack itemStack, Player player, int cooldown, int quietCooldown, boolean usesBlocking) {
        this.name = name;
        this.itemStack = itemStack;
        this.player = player;
        this.cooldown = 20 * cooldown;
        this.silentSeconds = 20 * quietCooldown;
        this.usesBlocking = usesBlocking;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //Cooldown Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void startPowerCooldowns() {
        isCoolingDown = true;
        startPreCooldown();
        Common.tell(player, "&7You fired &a" + name + "&7.");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isDead()) { //might have to reset booleans to default when player dies
                    cancel();
                    return;
                }
                if (canStartCooldown()) {
                    cooldowns();
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(BaconBrawl.getInstance(), 0L, 0L);
    }

    public void startPreCooldown() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isDead()) {
                    cancel();
                    return;
                }
                setCanStartCooldown(true);

            }
        }.runTaskLater(BaconBrawl.getInstance(), getSilentSeconds());
    }

    private void cooldowns() {
        PlayerCache cache = PlayerCache.from(player);

        final double[] cooldowntimer = {this.cooldown};
        new BukkitRunnable() {

            @Override
            public void run() {
                //If power was reset, like when a Player dies, cooldowns are reset.
                if (resetPower) {
                    resetPower = false;
//                    isCoolingDown = false;
                    cancel();
                    return;
                }
                //If Countdown is done
                if (cooldowntimer[0] <= 0) {
                    cooldowntimer[0] = 0;
                    setCanStartCooldown(false);
                    if (cache.hasGame() && cache.getCurrentGameMode() == GameJoinMode.PLAYING) {
                        Common.tell(player, "&7You can use &a" + getName() + "&7.");
                        Remain.sendActionBar(player, "&a&l" + getName() + " Recharged");
                        CompSound.NOTE_SNARE_DRUM.play(player, 0.5f, 1f);
                    }
                    isCoolingDown = false;
                    cancel();
                    return;
                }

                //Countdown
                if (cache.hasGame() && cache.getCurrentGameMode() == GameJoinMode.PLAYING) {
                    cooldowntimer[0] -= 5;
                    timeLeftToCooldown = cooldowntimer[0];
                } else {
                    cancel();
                    return;
                }

            }
        }.runTaskTimer(BaconBrawl.getInstance(), 0L, 5L);
    }

    public void resetPower() {
        this.resetPower = true;
        this.isCoolingDown = false;
        this.timeLeftToCooldown = 0.0;
        setCanStartCooldown(false);
    }

    public boolean canStartCooldown() {
        return canStartCooldown;
    }

    public double getConvertedTimeLeftCooldown() {
        return this.timeLeftToCooldown / 20;
    }

    private void setCanStartCooldown(boolean canStartCooldown) {
        this.canStartCooldown = canStartCooldown;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //Misc Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void givePowerItem() {
        player.getInventory().addItem(itemStack);
    }

    public void activatePower() {
    }

    public void postActivatedProjectile(LivingEntity victim, Projectile projectile) {
    }

    public void postActivatedMelee(LivingEntity victim) {
    }

    public void postActivatedMelee(LivingEntity victim, EntityDamageByEntityEvent event) {
    }

    public void startBlocking() {
        PlayerCache cache = PlayerCache.from(player);
        ItemStack shield = ItemCreator.of(CompMaterial.SHIELD, "Blocking", "This item does nothing but slow you down when blocking.").make();
        player.getInventory().setItemInOffHand(shield);
        cache.setCurrentlyBlocking(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player == null || player.isDead() || !player.isHandRaised()) {
                    player.getInventory().setItemInOffHand(CompMaterial.AIR.toItem());
                    cache.setCurrentlyBlocking(false);
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(BaconBrawl.getInstance(), 5L, 0L);
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //Static Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static void setToProjectileDamage(Player victim, int damage) {
        Snowball snowball = (Snowball) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.SNOWBALL);
        ProjectileHitEvent projEvent = new ProjectileHitEvent(snowball, victim, victim.getLocation().getBlock());
        EntityDamageEvent hitEvent = new EntityDamageEvent(victim, EntityDamageEvent.DamageCause.PROJECTILE, damage);
        victim.damage(damage);
        victim.setLastDamageCause(hitEvent);
        Bukkit.getServer().getPluginManager().callEvent(projEvent);
    }
}
