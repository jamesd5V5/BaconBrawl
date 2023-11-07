package org.mammothplugins.baconBrawl.model.baconbrawl.kits.nms;

import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@Getter
public class NmsDisguise {

    private static ArrayList<MobDisguise> disguises = new ArrayList<>();

    public static Disguise setDisguise(Player player, DisguiseType type) {
        MobDisguise disguise = new MobDisguise(type);
        disguise.setHearSelfDisguise(false);
        disguise.setViewSelfDisguise(false);
        disguise.setKeepDisguiseOnPlayerDeath(true);
        disguise.setModifyBoundingBox(true);
        disguise.setVelocitySent(true);

        DisguiseAPI.setActionBarShown(player, false);

        DisguiseAPI.disguiseToAll(player, disguise);
        disguises.add(disguise);
        return disguise;
    }

    public static boolean isDisguised(Player player) {
        return DisguiseAPI.isDisguised(player);
    }

    public static void removeDisguise(Player player) {
        DisguiseAPI.undisguiseToAll(player);
    }
}
