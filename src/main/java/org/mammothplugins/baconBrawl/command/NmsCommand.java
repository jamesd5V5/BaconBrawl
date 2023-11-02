package org.mammothplugins.baconBrawl.command;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.mammothplugins.baconBrawl.model.ssm.kits.nms.NmsDisguise;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommand;

import java.util.ArrayList;

public class NmsCommand extends SimpleCommand {
    public ArrayList<LivingEntity> creepers = new ArrayList<>();

    public NmsCommand() {
        super("nms");

        setMinArguments(1);
    }

    @Override
    public void onCommand() {
        if (args[0].equals("sheep")) {
            Player player = getPlayer();
            player.getWorld().spawn(player.getLocation(), Sheep.class);
        } else if (args[0].equals("check"))
            Common.broadcast("Is Disguised: " + NmsDisguise.isDisguised(getPlayer()));
        else if (args[0].equals("remove")) {
            NmsDisguise.removeDisguise(getPlayer());
            Common.broadcast("Removed");

        } else if (args[0].equals("creeper")) {
            DisguiseType type = DisguiseType.CHICKEN;
            Common.broadcast("DIsguised as " + type.name());
            NmsDisguise.setDisguise(getPlayer(), type);
        }
    }

//    @Override
//    protected List<String> tabComplete() {
//        return "creeper";
//    }
}
