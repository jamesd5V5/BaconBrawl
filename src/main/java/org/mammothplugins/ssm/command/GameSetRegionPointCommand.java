package org.mammothplugins.ssm.command;

import org.bukkit.event.inventory.ClickType;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mammothplugins.ssm.tool.RegionTool;

import java.util.List;

final class GameSetRegionPointCommand extends GameSubCommand {

    public GameSetRegionPointCommand() {
        super("setregionpoint/srp");

        this.setMinArguments(1);
        this.setUsage("<primary/secondary>");
        this.setDescription("Sets a region point in air while editing a game.");
        this.setPermission("ssm.cmd.admin.region");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        final PlayerCache cache = getCache();
        this.checkBoolean(cache.getCurrentGameMode() == GameJoinMode.EDITING, "You are not editing any game right now!");

        final ClickType clickType;

        switch (args[0]) {
            case "primary":
                clickType = ClickType.LEFT;
                break;

            case "secondary":
                clickType = ClickType.RIGHT;
                break;

            default: {
                returnTell("Unrecognized click '{0}', either type 'primary' or 'secondary'.");

                return;
            }
        }

        RegionTool.getInstance().handleBlockClick(getPlayer(), clickType, getPlayer().getLocation().getBlock());
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? completeLastWord("primary", "secondary") : NO_COMPLETE;
    }
}
