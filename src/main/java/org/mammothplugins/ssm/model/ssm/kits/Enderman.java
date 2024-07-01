package org.mammothplugins.ssm.model.ssm.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.EndermanWatcher;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mammothplugins.ssm.PlayerCache;
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
import java.util.stream.IntStream;

public class Enderman extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private EndermanWatcher endermanWatcher;

    public Enderman() {
        setName("Enderman");
        setLore(new String[]{"Just one Fricking WIll Smith\n" + "Smack yo face!"});
        setChatColor(ChatColor.DARK_PURPLE);
        setCompMaterial(CompMaterial.ENDER_PEARL);
        setDamage(7);
        setRegen(0.25);
        setKnockback(130);
    }

    @Override
    public void usePower(Player player, String powerName) {
        super.usePower(player, powerName);

        if (powerName == "Block Toss")
            kitPowers.get(player.getUniqueId()).get(0).activatePower();
        if (powerName == "Blink Teleport")
            kitPowers.get(player.getUniqueId()).get(1).activatePower();
    }


    @Override
    public void applyAttributes(Player player) {
        super.applyAttributes(player);

        applyArmor(player);
        givePowers(player);

        Disguise disguise = NmsDisguise.setDisguise(player, DisguiseType.ENDERMAN);
        this.endermanWatcher = (EndermanWatcher) disguise.getWatcher();
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        BlockToss blockToss = new BlockToss(player);
        powers.add(blockToss);
        addPower(blockToss, player);
        blockToss.givePowerItem();

        BlinkTeleport blinkTeleport = new BlinkTeleport(player);
        powers.add(blinkTeleport);
        addPower(blinkTeleport, player);
        blinkTeleport.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }

    private void applyArmor(Player player) {
        ItemStack helmet = ItemCreator.of(CompMaterial.CHAINMAIL_HELMET, "&5&lGot some").make();
        ItemStack chestplate = ItemCreator.of(CompMaterial.CHAINMAIL_CHESTPLATE, "&5&lSexier").make();
        ItemStack pants = ItemCreator.of(CompMaterial.CHAINMAIL_LEGGINGS, "&5&lLegs than").make();
        ItemStack boots = ItemCreator.of(CompMaterial.CHAINMAIL_BOOTS, "&5&lYours can ever be").make();

        player.getEquipment().setHelmet(helmet);
        player.getEquipment().setChestplate(chestplate);
        player.getEquipment().setLeggings(pants);
        player.getEquipment().setBoots(boots);
    }

    public class BlockToss extends Power {

        private Player player = getPlayer();

        public BlockToss(Player player) {
            super("Block Toss", ItemCreator.of(CompMaterial.IRON_SWORD,
                    "&f&lBlock Toss").make(), player, 3, 1, true);
        }

        @Override
        public void activatePower() {
            PlayerCache cache = PlayerCache.from(player);
            final boolean[] canLaunchBlock = {false};
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isDead() && cache.isCurrentlyBlocking()) {
                        canLaunchBlock[0] = true;
                        CompSound.BLOCK_COMPARATOR_CLICK.play(player, 0.5f, 1f);
                        endermanWatcher.setItemInMainHand(ItemCreator.of(CompMaterial.JACK_O_LANTERN).make());
                    }
                }
            }.runTaskLater(SSM.getInstance(), 8L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead()) {
                        endermanWatcher.setItemInMainHand(CompMaterial.AIR.toItem());
                        cancel();
                        return;
                    }

                    if (cache.isCurrentlyBlocking() == false && canLaunchBlock[0]) {
                        Snowball proj = player.launchProjectile(Snowball.class);
                        proj.setVelocity(proj.getVelocity().multiply(0.8));
                        ItemStack thrownBlock = ItemCreator.of(CompMaterial.GRASS_BLOCK, "ThrownBlock").make();
                        proj.setItem(thrownBlock);
                        endermanWatcher.setItemInMainHand(CompMaterial.AIR.toItem());

                        FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 1, 0), CompMaterial.JACK_O_LANTERN.getMaterial().createBlockData());
                        fallingBlock.setDropItem(false);
                        fallingBlock.setCancelDrop(true);
                        fallingBlock.setHurtEntities(false);
                        CompMetadata.setTempMetadata(proj, "ThrownBlock");
                        proj.setPassenger(fallingBlock);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (proj == null || proj.isDead()) {
                                    fallingBlock.remove();
                                    cancel();
                                    return;
                                }
                            }
                        }.runTaskTimer(SSM.getInstance(), 0L, 0L);

                        cancel();
                    }

                }
            }.runTaskTimer(SSM.getInstance(), 0L, 0L);

        }

        @Override
        public void postActivatedProjectile(LivingEntity victim, Projectile projectile) {
            victim.damage(5);
            CompSound.ENTITY_ARROW_HIT_PLAYER.play(player, 0.5f, 1f);
            Vector vector = projectile.getVelocity().setY(0);
            victim.setVelocity(vector.multiply(1.25).add(new Vector(0, 0, 0)));
        }

        @Override
        public boolean canStartCooldown() {
            PlayerCache cache = PlayerCache.from(player);
            return cache.isCurrentlyBlocking() ? false : true;
        }
    }

    private class BlinkTeleport extends Power {

        private Player player = getPlayer();

        public BlinkTeleport(Player player) {
            super("Blink Teleport", ItemCreator.of(CompMaterial.IRON_AXE,
                    "&f&lBlink Teleport").make(), player, 8, 2);
        }

        @Override
        public void activatePower() {
            Location loc = player.getLocation();
            for (int i = 0; i < 20; i++) {
                CompParticle.PORTAL.spawn(RandomUtil.nextLocation(loc, 1, true));
                CompParticle.SMOKE_NORMAL.spawn(RandomUtil.nextLocation(loc, 1, true));
            }

            Location playerLoc = player.getLocation();
            Location inFront = playerLoc.clone().add(playerLoc.getDirection().multiply(15));

            Block block = inFront.getBlock();
            if (block.getType() != Material.AIR) {
                double stickToGroundHeight = IntStream.range(0, (int) Math.ceil(15))
                        .mapToObj(dy -> block.getRelative(0, dy, 0))
                        .filter(b -> b.getType() == Material.AIR)
                        .findFirst().orElse(block).getY();

                inFront.setY(stickToGroundHeight);
            }
            player.teleport(inFront);

            for (int i = 0; i < 20; i++) {
                CompParticle.PORTAL.spawn(RandomUtil.nextLocation(player.getLocation(), 1, true));
                CompParticle.SMOKE_NORMAL.spawn(RandomUtil.nextLocation(player.getLocation(), 1, true));
            }
            CompSound.ENDERMAN_TELEPORT.play(player.getLocation(), 0.6f, 1f);
        }
    }
}
