package org.mammothplugins.baconBrawl.model.baconbrawl.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.PigWatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mammothplugins.baconBrawl.BaconBrawl;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.baconbrawl.BaconBrawlCore;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.nms.NmsDisguise;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers.Power;
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

public class MamaPiggles extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private PigWatcher pigWatcher;
    private Wolf piglet;

    public MamaPiggles() {
        setName("MamaPiggles");
        setLore(new String[]{"Mama & Baby Piggles fight together\n" + "Throw a Bacon Blast,"});

        setChatColor(ChatColor.RED);

        setCompMaterial(CompMaterial.PORKCHOP);

        setKnockBack(1);
    }

    @Override
    public void usePower(Player player, String powerName) {
        super.usePower(player, powerName);

        if (powerName == "Bacon Blast")
            kitPowers.get(player.getUniqueId()).get(0).activatePower();
    }


    @Override
    public void applyAttributes(Player player) {
        super.applyAttributes(player);

        givePowers(player);

        Disguise disguise = NmsDisguise.setDisguise(player, DisguiseType.PIG);
        this.pigWatcher = (PigWatcher) disguise.getWatcher();
        this.pigWatcher.setBaby();


//        this.piglet = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
//        piglet.setOwner(player);
//        piglet.setInvulnerable(true);
//
//        player.setPassenger(piglet);
//
//        Disguise babyDisguise = NmsDisguise.setDisguise(piglet, DisguiseType.PIG);
//        new EntityHider
//        PigWatcher babyWatcher = (PigWatcher) disguise.getWatcher();
//        babyWatcher.setCustomName("&d&l" + player.getName() + "'s Piglet");
//        babyWatcher.setBaby();
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        BaconBlast baconBlast = new BaconBlast(player);
        powers.add(baconBlast);
        addPower(baconBlast, player);
        baconBlast.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }

    @Override
    public void onDeath(Player player) {
        super.onDeath(player);

        if (piglet != null && piglet.isDead())
            piglet.remove();
    }

    public class BaconBlast extends Power {

        private Player player = getPlayer();

        public BaconBlast(Player player) {
            super("Bacon Blast",
                    ItemCreator.of(CompMaterial.IRON_AXE, "&f&lBacon Blast").make(), player, 3);
        }

        @Override
        public void activatePower() {
            Snowball proj = player.launchProjectile(Snowball.class);
            ItemStack porkStack = ItemCreator.of(CompMaterial.PORKCHOP, "PorkBomb").make();
            proj.setItem(porkStack);
            CompMetadata.setTempMetadata(proj, "PorkBomb");
            //player.setVelocity(new Vector(player.getVelocity().getX() * -2, 1, player.getVelocity().getZ() * -2));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (proj.isDead()) {
                        cancel();
                        return;
                    }
                    for (int i = 0; i < 1; i++)
                        CompParticle.HEART.spawn(RandomUtil.nextLocation(proj.getLocation(), 0.5, true));
                }
            }.runTaskTimer(BaconBrawl.getInstance(), 0, 3L);
        }

        @Override
        public void postActivatedProjectile(LivingEntity victim, Projectile projectile) {
            BaconBrawlCore bbc = (BaconBrawlCore) PlayerCache.from(player).getCurrentGame();
            double value = 0;
            if (bbc.isPorkalypseMode())
                value = 0.75;
            victim.damage(1);
            float pitch = (float) (0.8 - value);
            float pitch2 = (float) (1 - value);
            CompSound.EXPLODE.play(projectile.getLocation(), 0.5f, pitch);
            CompSound.ENTITY_ARROW_HIT_PLAYER.play(player, 0.5f, pitch2);
            Vector vector = projectile.getVelocity().setY(0);
            victim.setVelocity(vector.multiply(1.2 + value).add(new Vector(0, 1.0, 0)));

            PlayerCache vCache = PlayerCache.from((Player) victim);
            vCache.setPotentialKiller(player);

            Common.runLater(20 * 5, () -> {
                vCache.setPotentialKiller(null);
            });
        }
    }
}
