package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.tool.GameTool;
import org.mineacademy.fo.menu.MenuTools;

import java.util.List;

final class GameToolsCommand extends GameSubCommand {

    GameToolsCommand() {
        super("tools/t");

        this.setDescription("Open the Game Tools menu.");
        this.setPermission("baconbrawl.cmd.admin.tools");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        MenuTools.of(GameTool.class).displayTo(getPlayer());
    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
