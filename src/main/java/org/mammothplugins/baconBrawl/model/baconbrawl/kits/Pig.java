package org.mammothplugins.baconBrawl.model.baconbrawl.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mammothplugins.baconBrawl.BaconBrawl;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.nms.NmsDisguise;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers.Power;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Pig extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private SheepWatcher sheepWatcher;


    public Pig() {
        setName("Pig");
        setLore(new String[]{"\"...Oink?\"", "Briefly become invisible,", "deal %250 more knockback", "when behind your enemy."});
        setChatColor(ChatColor.LIGHT_PURPLE);
        setCompMaterial(CompMaterial.PINK_WOOL);
        setKnockBack(1);
    }

    @Override
    public void usePower(Player player, String powerName) {
        super.usePower(player, powerName);

        if (powerName == "Cloak")
            kitPowers.get(player.getUniqueId()).get(0).activatePower();
    }


    @Override
    public void applyAttributes(Player player) {
        super.applyAttributes(player);

        givePowers(player);

        Disguise disguise = NmsDisguise.setDisguise(player, DisguiseType.SHEEP);
        this.sheepWatcher = (SheepWatcher) disguise.getWatcher();
        sheepWatcher.setColor(DyeColor.PINK);
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        CloakPower cloakPower = new CloakPower(player);
        powers.add(cloakPower);
        addPower(cloakPower, player);
        cloakPower.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }


    public class CloakPower extends Power {

        private Player player = getPlayer();
        private boolean hasBeenRevealed = true;

        public CloakPower(Player player) {
            super("Cloak",
                    ItemCreator.of(CompMaterial.IRON_AXE, "&f&lCloak").make(), player, 10);
        }

        @Override
        public void activatePower() {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 4 * 20, 50));
            CompSound.SHEEP_SHEAR.play(player.getLocation(), 0.5f, 0.8f);
            hasBeenRevealed = false;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (hasBeenRevealed == false) {
                        Common.tell(player, "You are no longer invisible.");
                        CompSound.ENTITY_SHEEP_DEATH.play(player.getLocation(), 0.5f, 1.7f);
                    }
                    hasBeenRevealed = true;
                }
            }.runTaskLater(BaconBrawl.getInstance(), 20 * 4);
        }

        public boolean canPostActiavteMelee() {
            return this.isCoolingDown() && hasBeenRevealed == false;
        }

        @Override
        public void postActivatedMelee(LivingEntity victim, EntityDamageByEntityEvent event) {
            if (canPostActiavteMelee()) {
                hasBeenRevealed = true;
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                CompSound.ENTITY_SHEEP_DEATH.play(player.getLocation(), 0.5f, 1.7f);
                victim.damage(1);
                Vector betweenVector = victim.getEyeLocation().toVector().subtract(player.getEyeLocation().toVector());
                Vector direction = victim.getEyeLocation().getDirection();
                double delta = betweenVector.dot(direction);
                if (delta > 0) { //if the player is Behind the victim's field of vision
                    Common.tell(player, "You successfully launched " + victim.getName() + ".");
                    event.getEntity().setVelocity(player.getLocation().getDirection().setY(0).normalize().multiply(getKnockBack() * 2.5).add(new Vector(0, 0.7, 0)));
                }

                PlayerCache.from((Player) victim).startCountdownLastKiller(player);
            }
        }

        public void revealPlayer(Player player) {
            if (hasBeenRevealed == false) {
                CompSound.FIZZ.play(player.getLocation(), 0.5f, 1.7f);
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                Common.tell(player, "You are no longer invisible.");
                hasBeenRevealed = true;
            }
        }
    }
}
