package org.mammothplugins.baconBrawl.model.baconbrawl.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.PigWatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mammothplugins.baconBrawl.BaconBrawl;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.nms.NmsDisguise;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers.Power;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ElMuchachoPig extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private PigWatcher pigWatcher;

    public ElMuchachoPig() {
        setName("ElMuchachoPig"); //basic
        setChatColor(ChatColor.LIGHT_PURPLE);
        setCompMaterial(CompMaterial.IRON_AXE);
        setKnockBack(1);
    }

    @Override
    public void usePower(Player player, String powerName) {
        super.usePower(player, powerName);

        if (powerName == "Body Slam")
            kitPowers.get(player.getUniqueId()).get(0).activatePower();
    }


    @Override
    public void applyAttributes(Player player) {
        super.applyAttributes(player);

        givePowers(player);

        Disguise disguise = NmsDisguise.setDisguise(player, DisguiseType.PIG);
        this.pigWatcher = (PigWatcher) disguise.getWatcher();
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        BodySlamPower bodySlamPower = new BodySlamPower(player);
        powers.add(bodySlamPower);
        addPower(bodySlamPower, player);
        bodySlamPower.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }


    public class BodySlamPower extends Power {

        private Player player = getPlayer();
        private boolean hasPostLaunched = false;
        private boolean canNoLongerDashTouch = false;

        public BodySlamPower(Player player) {
            super("Body Slam",
                    ItemCreator.of(CompMaterial.IRON_AXE, "&f&lBody Slam").make(), player, 6);
        }

        @Override
        public void activatePower() {
            player.setVelocity(player.getEyeLocation().getDirection().multiply(1.3));
            canNoLongerDashTouch = false;
            CompSound.HORSE_DEATH.play(player, 0.5f, 1.7f);
            AtomicBoolean showParticles = new AtomicBoolean(true);
            Common.runLater(1 * 20, () -> {
                canNoLongerDashTouch = true;
                showParticles.set(false);
            });

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isDead() || showParticles.get() == false) {
                        cancel();
                    }
                    for (int i = 0; i < 2; i++)
                        CompParticle.LAVA.spawn(RandomUtil.nextLocation(player.getLocation(), 1, true));
                }
            }.runTaskTimer(BaconBrawl.getInstance(), 0, 3L);
        }

        @Override
        public void postActivatedMelee(LivingEntity victim) {
            if (this.isCoolingDown() && canNoLongerDashTouch == false && hasPostLaunched == false) {
                victim.damage(1);
                Vector vector = player.getVelocity().setY(0);
                victim.setVelocity(vector.multiply(2.5).add(new Vector(0, 0.5, 0)));
                player.setVelocity(vector.multiply(-0.2).add(new Vector(0, 0.5, 0)));
                hasPostLaunched = true;

                Common.runLater(6 * 20 + 20 + 2, () -> { //6 for the cooldown, 20 for the quiet cooldown
                    hasPostLaunched = false;
                    canNoLongerDashTouch = false;
                });

                PlayerCache vCache = PlayerCache.from((Player) victim);
                vCache.setPotentialKiller(player);
                BaconBrawl baconBrawl = (BaconBrawl) vCache.getCurrentGame();
                Common.runLater(20 * 5, () -> {
                    vCache.setPotentialKiller(null);
                });
            }
        }
    }
}
