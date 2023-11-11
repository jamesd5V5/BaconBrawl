package org.mammothplugins.baconBrawl.model.baconbrawl.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.PigWatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.nms.NmsDisguise;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers.Power;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MamaPiggles extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private PigWatcher pigWatcher;

    public MamaPiggles() {
        setName("MamaPiggles");
        setChatColor(ChatColor.RED);
        setCompMaterial(CompMaterial.IRON_AXE);
        setKnockBack(3);
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
    }

    private void givePowers(Player player) {
        ArrayList<Power> powers = new ArrayList<>();

        BaconBlast baconBlast = new BaconBlast(player);
        powers.add(baconBlast);
        addPower(baconBlast, player);
        baconBlast.givePowerItem();

        kitPowers.put(player.getUniqueId(), powers);
    }


    public class BaconBlast extends Power {

        private Player player = getPlayer();

        public BaconBlast(Player player) {
            super("Bacon Blast",
                    ItemCreator.of(CompMaterial.IRON_AXE, "&f&lBacon Blast").make(), player, 4);
        }

        @Override
        public void activatePower() {
            Snowball proj = player.launchProjectile(Snowball.class);
            ItemStack porkStack = ItemCreator.of(CompMaterial.PORKCHOP, "PorkBomb").make();
            proj.setItem(porkStack);
            CompMetadata.setTempMetadata(proj, "PorkBomb");
        }

        @Override
        public void postActivatedProjectile(LivingEntity victim, Projectile projectile) {
            CompSound.EXPLODE.play(projectile.getLocation(), 0.5f, 0.8f);
            CompSound.ENTITY_ARROW_HIT_PLAYER.play(player, 0.5f, 1f);
            Vector vector = projectile.getVelocity().setY(0);
            victim.setVelocity(vector.multiply(1.8).add(new Vector(0, 1.0, 0)));
        }
    }
}
