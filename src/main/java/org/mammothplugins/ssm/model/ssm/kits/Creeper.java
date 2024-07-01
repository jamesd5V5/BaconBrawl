package org.mammothplugins.ssm.model.ssm.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
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

public class Creeper extends org.mammothplugins.ssm.model.ssm.kits.Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private CreeperWatcher creeperWatcher;
    private boolean isPowered = false;
    private boolean canBePowered = true;

    public Creeper() {
        setName("Creeper"); //basic
        setLore(new String[]{"Ssso you think you can dancccee\n" + "KABOOM"});
        setChatColor(ChatColor.GREEN);
        setCompMaterial(CompMaterial.TNT);
        setDamage(6);
        setRegen(0.4);
        setKnockback(165);
    }

    @Override
    public void usePower(Player player, String powerName) {
        super.usePower(player, powerName);

        if (powerName == "Sulphur Bomb")
            kitPowers.get(player.getUniqueId()).get(0).activatePower();
        if (powerName == "Explode")
            kitPowers.get(player.getUniqueId()).get(1).activatePower();
    }


    @Override
    public void applyAttributes(Player player) {
        super.applyAttributes(player);

        applyArmor(player);
        givePowers(player);

        Disguise disguise = NmsDisguise.setDisguise(player, DisguiseType.CREEPER);
        this.creeperWatcher = (CreeperWatcher) disguise.getWatcher();
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        SulpherBombPower sulpherBombPower = new SulpherBombPower(player);
        powers.add(sulpherBombPower);
        addPower(sulpherBombPower, player);
        sulpherBombPower.givePowerItem();

        ExplodePower explodePower = new ExplodePower(player);
        powers.add(explodePower);
        addPower(explodePower, player);
        explodePower.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }

    private void applyArmor(Player player) {
        ItemStack helmet = ItemCreator.of(CompMaterial.LEATHER_HELMET, "&a&lSsssss").make();
        ItemStack chestplate = ItemCreator.of(CompMaterial.LEATHER_CHESTPLATE, "&a&lCreeper").make();
        ItemStack pants = ItemCreator.of(CompMaterial.LEATHER_LEGGINGS, "&a&lAwwwww").make();
        ItemStack boots = ItemCreator.of(CompMaterial.IRON_BOOTS, "&a&lMan").make();

        LeatherArmorMeta hmeta = (LeatherArmorMeta) helmet.getItemMeta();
        hmeta.setColor(Color.GREEN);
        helmet.setItemMeta(hmeta);
        LeatherArmorMeta cmeta = (LeatherArmorMeta) chestplate.getItemMeta();
        cmeta.setColor(Color.GREEN);
        chestplate.setItemMeta(cmeta);
        LeatherArmorMeta pmeta = (LeatherArmorMeta) pants.getItemMeta();
        pmeta.setColor(Color.GREEN);
        pants.setItemMeta(pmeta);

        player.getEquipment().setHelmet(helmet);
        player.getEquipment().setChestplate(chestplate);
        player.getEquipment().setLeggings(pants);
        player.getEquipment().setBoots(boots);
    }

    @Override
    public void setOnFire(Player player, int ticks) {
        super.setOnFire(player, ticks);
        creeperWatcher.setIgnited(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                creeperWatcher.setIgnited(false);
            }
        }.runTaskLater(SSM.getInstance(), ticks);
    }

    public void onProjectileHit(Player player) {
        if (!canBePowered)
            return;

        creeperWatcher.setPowered(true);
        isPowered = true;
        canBePowered = false;

        for (int i = 0; i < 10; i++)
            CompParticle.FIREWORKS_SPARK.spawn(RandomUtil.nextLocation(player.getLocation(), 1, true));
        new BukkitRunnable() {
            @Override
            public void run() {
                creeperWatcher.setPowered(false);
                isPowered = false;
                if (player.isDead()) {
                    cancel();
                    return;
                }
            }
        }.runTaskLater(SSM.getInstance(), 50L);
        new BukkitRunnable() {
            @Override
            public void run() {
                canBePowered = true;
            }
        }.runTaskLater(SSM.getInstance(), 100L);
    }

    public void onMeleeAttack(Player victim) {
        if (!isPowered)
            return;
        isPowered = false;
        creeperWatcher.setPowered(false);
        final boolean[] hasSufferedEnough = {false};
        Location savedLocation = victim.getLocation();
        Float yaw = savedLocation.getYaw();
        Float pitch = savedLocation.getPitch();

        victim.getWorld().strikeLightning(victim.getLocation());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (victim.isDead() || hasSufferedEnough[0]) {
                    cancel();
                    return;
                }
                Location loc = victim.getLocation();
                Float yaw = Float.valueOf(RandomUtil.nextInt(360));
                Float pitch = Float.valueOf(RandomUtil.nextInt(45));
                victim.teleport(new Location(victim.getWorld(), loc.getX(), loc.getY(), loc.getZ(), yaw, pitch));

                CompSound.ENTITY_PLAYER_HURT.play(victim.getLocation(), 0.5f, 1f);


            }
        }.runTaskTimer(SSM.getInstance(), 0, 0L);

        new BukkitRunnable() {
            @Override
            public void run() {
                hasSufferedEnough[0] = true;
                Location currentLocation = victim.getLocation();
                victim.teleport(new Location(victim.getWorld(), currentLocation.getX(), currentLocation.getY(), currentLocation.getZ(), yaw, pitch));
            }
        }.runTaskLater(SSM.getInstance(), 20L);
    }

    @Override
    public void onDeath(Player player) {
        super.onDeath(player);

        isPowered = false;
        canBePowered = true;
        if (creeperWatcher != null) {
            creeperWatcher.setIgnited(false);
            creeperWatcher.setPowered(false);
        }

    }

    public class SulpherBombPower extends Power {

        private Player player = getPlayer();

        public SulpherBombPower(Player player) {
            super("Sulphur Bomb",
                    ItemCreator.of(CompMaterial.IRON_SWORD, "&f&lSulphur Bomb").make(), player, 3, 1);
        }

        @Override
        public void activatePower() {
            Snowball proj = player.launchProjectile(Snowball.class);
            //proj.setVelocity(proj.getVelocity().multiply(1.25));
            ItemStack coalItemStack = ItemCreator.of(CompMaterial.COAL, "SulpherBomb").make();
            proj.setItem(coalItemStack);
            CompMetadata.setTempMetadata(proj, "SulpherBomb");

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (proj.isDead()) {
                        cancel();
                        return;
                    }
                    for (int i = 0; i < 1; i++)
                        CompParticle.SMOKE_NORMAL.spawn(RandomUtil.nextLocation(proj.getLocation(), 0.5, true));
                }
            }.runTaskTimer(SSM.getInstance(), 0, 3L);
        }

        @Override
        public void postActivatedProjectile(LivingEntity victim, Projectile projectile) {
            victim.damage(5);
            CompSound.EXPLODE.play(projectile.getLocation(), 0.5f, 1f);
            CompSound.ENTITY_ARROW_HIT_PLAYER.play(player, 0.5f, 1f);
            Vector vector = projectile.getVelocity().setY(0);
            victim.setVelocity(vector.multiply(1.8).add(new Vector(0, 1.0, 0)));

            PlayerCache vCache = PlayerCache.from((Player) victim);
            vCache.setPotentialKiller(player);

            Common.runLater(20 * 5, () -> {
                vCache.setPotentialKiller(null);
            });
        }
    }

    private class ExplodePower extends Power {

        private Player player = getPlayer(); //can't be one player, gonna have to rethink this

        public ExplodePower(Player player) {
            super("Explode",
                    ItemCreator.of(CompMaterial.IRON_SHOVEL, "&f&lExplode").make(), player, 6, 2);
        }

        @Override
        public void activatePower() {
            Vector vector = new Vector(0, 0, 0);
            player.setVelocity(vector);
            final boolean[] hasLaunched = {false};
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead() || hasLaunched[0]) {
                        cancel();
                        return;
                    }
                    player.setVelocity(vector);
                    CompSound.CREEPER_HISS.play(player.getLocation(), 0.25f, 1f);
                    creeperWatcher.setIgnited(true);


                }
            }.runTaskTimer(SSM.getInstance(), 0, 0L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    creeperWatcher.setIgnited(false);
                    if (player.isDead()) {
                        cancel();
                        return;
                    }

                    for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                        if (entity instanceof LivingEntity) {
                            Vector launchDirection = entity.getLocation().toVector().add(player.getLocation().toVector().multiply(-1));
                            launchDirection.setY(1.25);
                            entity.setVelocity(launchDirection);
                            ((LivingEntity) entity).damage(4);

                        }
                    }

                    CompSound.EXPLODE.play(player.getLocation());
                    player.setVelocity(player.getEyeLocation().getDirection().multiply(2));
                    hasLaunched[0] = true;


                }
            }.runTaskLater(SSM.getInstance(), 30L);
        }
    }
}
