package org.mammothplugins.baconBrawl.model.ssm.kits;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.PigWatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.model.ssm.kits.nms.NmsDisguise;
import org.mammothplugins.baconBrawl.model.ssm.kits.powers.Power;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ElMuchachoPig extends Kits {

    private HashMap<UUID, ArrayList<Power>> kitPowers = new HashMap<>();
    private PigWatcher pigWatcher;

    public ElMuchachoPig() {
        setName("ElMuchachoPig"); //basic
        setChatColor(ChatColor.LIGHT_PURPLE);
        setCompMaterial(CompMaterial.IRON_AXE);
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

        public BodySlamPower(Player player) {
            super("Body Slam",
                    ItemCreator.of(CompMaterial.IRON_AXE, "&f&lBody Slam").make(), player, 6);
        }

        @Override
        public void activatePower() {
            player.setVelocity(player.getEyeLocation().getDirection().multiply(2));
        }
    }
}
