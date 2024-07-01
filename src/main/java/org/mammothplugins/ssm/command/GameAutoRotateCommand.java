package org.mammothplugins.ssm.command;

import org.mineacademy.fo.Common;

import java.util.List;

final class GameAutoRotateCommand extends GameSubCommand {

    GameAutoRotateCommand() {
        super("autorotate/ar");

        this.setDescription("Set whether the game should auto rotate.");
        this.setPermission("ssm.cmd.admin.autorotate");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();
        Common.tell(getPlayer(), "&cThis feature is currently disbaled.");

//        Game game = PlayerCache.from(getPlayer()).getCurrentGame();
//
//        if (game == null)
//            Messenger.error(getPlayer(), "You must be editing a game to use this command!");
//        else {
//            if (game.isAutoRotate())
//                game.setAutoRotate(false);
//            else
//                game.setAutoRotate(true);
//            CompSound.CAT_MEOW.play(getPlayer(), 5.0F, 0.3F);
//            Common.tell(getPlayer(), "&7AutoRotate has been set to " + (game.isAutoRotate() ? "&aTrue" : "&cFalse") + "&7.");
//        }

    }

    @Override
    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
