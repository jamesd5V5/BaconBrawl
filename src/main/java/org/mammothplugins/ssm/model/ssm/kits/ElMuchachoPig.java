package org.mammothplugins.ssm.model.ssm.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.PigWatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.SSM;
import org.mammothplugins.ssm.model.ssm.SSMCore;
import org.mammothplugins.ssm.model.ssm.kits.nms.NmsDisguise;
import org.mammothplugins.ssm.model.ssm.kits.powers.Power;
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
        setLore(new String[]{"Such a fat pig. Oink\n" + "Slam yourself forward."});
        setChatColor(ChatColor.LIGHT_PURPLE);
        setCompMaterial(CompMaterial.IRON_AXE);
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
            SSMCore bbc = (SSMCore) PlayerCache.from(player).getCurrentGame();
            double value = 0;
            player.setVelocity(player.getEyeLocation().getDirection().multiply(1.3 + value));
            canNoLongerDashTouch = false;
            float pitch = (float) (1.7 - value);
            CompSound.HORSE_DEATH.play(player, 0.5f, pitch);
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
            }.runTaskTimer(SSM.getInstance(), 0, 3L);
        }

        @Override
        public void postActivatedMelee(LivingEntity victim) {
            if (this.isCoolingDown() && canNoLongerDashTouch == false && hasPostLaunched == false) {
                SSMCore bbc = (SSMCore) PlayerCache.from(player).getCurrentGame();
                double value = 0;
                victim.damage(1);
                Vector vector = player.getVelocity().setY(0);
                victim.setVelocity(vector.multiply(2.5 + value).add(new Vector(0, 0.5, 0)));
                player.setVelocity(vector.multiply(-0.2).add(new Vector(0, 0.5, 0)));
                hasPostLaunched = true;

                Common.runLater(6 * 20 + 20 + 2, () -> { //6 for the cooldown, 20 for the quiet cooldown
                    hasPostLaunched = false;
                    canNoLongerDashTouch = false;
                });
                PlayerCache.from((Player) victim).startCountdownLastKiller(player);
            }
        }
    }
}
