package org.mammothplugins.ssm.model.ssm.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.WitherSkeletonWatcher;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

public class WitherSkeleton extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private WitherSkeletonWatcher witherSkeletonWatcher;
    private HashMap<UUID, WitherSkull> witherSkull = new HashMap<>();
    private HashMap<UUID, org.bukkit.entity.WitherSkeleton> image = new HashMap<>();

    private boolean pastSwapCooldown = true;
    private boolean justLaunchedImage = false;

    public WitherSkeleton() {
        setName("WitherSkeleton");
        setLore(new String[]{"And they were\n" + "TWINS!"});
        setChatColor(ChatColor.DARK_GRAY);
        setCompMaterial(CompMaterial.WITHER_SKELETON_SKULL);
        setDamage(6);
        setRegen(0.3);
        setKnockback(120);
    }

    @Override
    public void usePower(Player player, String powerName) {
        super.usePower(player, powerName);
        if (powerName == "Guided Wither Skull")
            kitPowers.get(player.getUniqueId()).get(0).activatePower();

        if (powerName == "Wither Image")
            kitPowers.get(player.getUniqueId()).get(1).activatePower();
    }


    @Override
    public void applyAttributes(Player player) {
        super.applyAttributes(player);

        applyArmor(player);
        givePowers(player);

        Disguise disguise = NmsDisguise.setDisguise(player, DisguiseType.WITHER_SKELETON);
        this.witherSkeletonWatcher = (WitherSkeletonWatcher) disguise.getWatcher();
        witherSkeletonWatcher.setEnraged(true);
        ItemStack itemStack = CompMaterial.AIR.toItem();
        witherSkeletonWatcher.setArmor(new ItemStack[]{itemStack, itemStack, itemStack, itemStack});
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        GuidedWitherSkullPower guidedWitherSkullPower = new GuidedWitherSkullPower(player);
        powers.add(guidedWitherSkullPower);
        addPower(guidedWitherSkullPower, player);
        guidedWitherSkullPower.givePowerItem();

        WitherImagePower witherImagePower = new WitherImagePower(player);
        powers.add(witherImagePower);
        addPower(witherImagePower, player);
        witherImagePower.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }

    private void applyArmor(Player player) {
        ItemStack helmet = ItemCreator.of(CompMaterial.CHAINMAIL_HELMET, "&8&lSpooke").make();
        ItemStack chestplate = ItemCreator.of(CompMaterial.CHAINMAIL_CHESTPLATE, "&8&lSeason").make();
        ItemStack pants = ItemCreator.of(CompMaterial.CHAINMAIL_LEGGINGS, "&8&lApproaches").make();
        ItemStack boots = ItemCreator.of(CompMaterial.CHAINMAIL_BOOTS, "&8&lRattttllle").make();

        player.getEquipment().setHelmet(helmet);
        player.getEquipment().setChestplate(chestplate);
        player.getEquipment().setLeggings(pants);
        player.getEquipment().setBoots(boots);
    }

    public boolean canGuideWitherSkull(Player player) {
        if (witherSkull.get(player.getUniqueId()) == null || witherSkull.get(player.getUniqueId()).isDead())
            return false;
        return true;
    }

    public boolean canSwap(Player player) {
        if (image.get(player.getUniqueId()) == null || image.get(player.getUniqueId()).isDead())
            return false;
        if (pastSwapCooldown == false)
            return false;
        if (!PlayerCache.from(player).getCurrentGame().getRegion().isWithin(image.get(player.getUniqueId()).getLocation()) || justLaunchedImage)
            return false;
        return true;
    }

    public void onSwordGuide(Player player) {
        if (canGuideWitherSkull(player)) {
            Vector playerDirection = player.getEyeLocation().getDirection();
            witherSkull.get(player.getUniqueId()).setVelocity(playerDirection);
        }
    }


    public void swap(Player player) {
        double[] countdown = {4.0};
        if (canSwap(player)) {
            if (canSwap(player)) {
                Location imageLoc = image.get(player.getUniqueId()).getLocation();
                Location playerLoc = player.getLocation();

                image.get(player.getUniqueId()).teleport(playerLoc);
                player.teleport(imageLoc);
                for (int i = 0; i < 10; i++) {
                    CompParticle.PORTAL.spawn(RandomUtil.nextLocation(image.get(player.getUniqueId()).getLocation(), 1, true));
                    CompParticle.PORTAL.spawn(RandomUtil.nextLocation(player.getLocation(), 1, true));
                }
                CompSound.ENDERMAN_TELEPORT.play(player, 0.75f, 1f);
            }
            pastSwapCooldown = false;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead()) {
                        pastSwapCooldown = true;
                        cancel();
                        return;
                    }

                    pastSwapCooldown = true;
                }
            }.runTaskLater(SSM.getInstance(), 40L);


            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead() || countdown[0] <= 0) {
                        cancel();
                        return;
                    }
                    countdown[0]--;

                }
            }.runTaskTimer(SSM.getInstance(), 0L, 20L);
        } else {
            if (image.get(player.getUniqueId()) != null && !image.get(player.getUniqueId()).isDead())
                Common.tell(player, "&7You cannot use &aswap &7for &a" + "1" + " Seconds&7."); //todo I mean count[0] works, just wont change for out here, but 1sec is pretty accurate
        }
    }

    @Override
    public void onDeath(Player player) {
        super.onDeath(player);

        if (image.get(player.getUniqueId()) == null)
            return;
        if (!image.get(player.getUniqueId()).isDead())
            image.get(player.getUniqueId()).remove();
    }

    public class GuidedWitherSkullPower extends Power {

        private Player player = getPlayer();

        public GuidedWitherSkullPower(Player player) {
            super("Guided Wither Skull", ItemCreator.of(CompMaterial.IRON_SWORD,
                    "&f&lGuided Wither Skull").make(), player, 3, 1, true);
        }

        @Override
        public void activatePower() {
            Vector playerDirection = player.getEyeLocation().getDirection();
            witherSkull.put(player.getUniqueId(), player.launchProjectile(WitherSkull.class));
            witherSkull.get(player.getUniqueId()).setVelocity(playerDirection.multiply(0.75));
            CompMetadata.setTempMetadata(witherSkull.get(player.getUniqueId()), "WitherSkull");
            CompSound.WITHER_SHOOT.play(witherSkull.get(player.getUniqueId()).getLocation(), 0.5f, 0.8f);

            UUID witherSkullUUID = witherSkull.get(player.getUniqueId()).getUniqueId();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (witherSkull.get(player.getUniqueId()) != null && !witherSkull.get(player.getUniqueId()).isDead())
                        if (witherSkullUUID.equals(witherSkull.get(player.getUniqueId()).getUniqueId()))
                            witherSkull.get(player.getUniqueId()).remove();
                }
            }.runTaskLater(SSM.getInstance(), 140L);
        }

        @Override
        public void postActivatedProjectile(LivingEntity victim, Projectile projectile) {
            for (Entity entity : projectile.getNearbyEntities(3, 3, 3))
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (!livingEntity.getUniqueId().equals(player.getUniqueId()))
                        livingEntity.damage(6);
                }
            CompSound.ENTITY_ARROW_HIT_PLAYER.play(player.getLocation(), 0.5f, 1f);
            victim.setVelocity(witherSkull.get(player.getUniqueId()).getVelocity().add(new Vector(0, 1.25, 0)));
            witherSkull.get(player.getUniqueId()).remove();
        }

        @Override
        public boolean canStartCooldown() {
            PlayerCache cache = PlayerCache.from(player);
//            if (cache.isCurrentlyBlocking())
//                return false;
            return !canGuideWitherSkull(player);
        }
    }

    private class WitherImagePower extends Power {

        private Player player = getPlayer();

        public WitherImagePower(Player player) {
            super("Wither Image", ItemCreator.of(CompMaterial.IRON_AXE,
                    "&f&lWither Image").make(), player, 10, 1);
        }

        @Override
        public void activatePower() {
            Vector playerDirection = player.getEyeLocation().getDirection();
            image.put(player.getUniqueId(), player.getWorld().spawn(player.getEyeLocation().add(0, -0.5, 0), org.bukkit.entity.WitherSkeleton.class));
            image.get(player.getUniqueId()).setCustomName(player.getName() + "'s image");
            image.get(player.getUniqueId()).setCustomNameVisible(true);

            image.get(player.getUniqueId()).setVelocity(playerDirection.multiply(2));
            CompSound.WITHER_SPAWN.play(image.get(player.getUniqueId()).getLocation());
            justLaunchedImage = true;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead()) {
                        if (image.get(player.getUniqueId()) != null)
                            image.get(player.getUniqueId()).remove();
                        cancel();
                        return;
                    }
                    for (int i = 0; i < 3; i++)
                        CompParticle.ASH.spawn(RandomUtil.nextLocation(image.get(player.getUniqueId()).getLocation(), 1, true));
                    if (image.get(player.getUniqueId()).getTarget() != null)
                        if (image.get(player.getUniqueId()).getTarget().getUniqueId().equals(player.getUniqueId()))
                            image.get(player.getUniqueId()).setTarget(null);
                    LivingEntity livingEntity = image.get(player.getUniqueId()).getTarget();
                    if (livingEntity == null)
                        for (Entity entity : image.get(player.getUniqueId()).getNearbyEntities(30, 30, 30))
                            if (entity instanceof LivingEntity && !entity.getUniqueId().equals(player.getUniqueId()))
                                livingEntity = (LivingEntity) entity;
                    if (livingEntity != null)
                        image.get(player.getUniqueId()).setTarget(livingEntity);
                }
            }.runTaskTimer(SSM.getInstance(), 0L, 2L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    justLaunchedImage = false;
                }
            }.runTaskLater(SSM.getInstance(), 20L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (image.get(player.getUniqueId()) != null)
                        image.get(player.getUniqueId()).remove();
                }
            }.runTaskLater(SSM.getInstance(), 140L);
        }

        @Override
        public boolean canStartCooldown() {
            if (image.get(player.getUniqueId()) == null)
                return true;
            if (image.get(player.getUniqueId()).isDead())
                return true;
            return false;
        }
    }
}
