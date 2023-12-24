package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameJoinMode;

import java.util.List;

final class GameSpectateCommand extends GameSubCommand {

    GameSpectateCommand() {
        super("spectate/sp");

        this.setDescription("Spectates an ongoing game.");
        this.setUsage("[name]");
        this.setPermission("baconbrawl.cmd.player.spectate");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        Game game = this.findGameFromLocationOrFirstArg();
        this.checkBoolean(game.isPlayed(), "You can only spectate games that are played.");
        this.checkBoolean(game.findPlayer(getPlayer()) == null, "You cannot spectate a game you are playing. Type '/" + getLabel() + " leave' to exit.");

        game.joinPlayer(this.getPlayer(), GameJoinMode.SPECTATING);
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}
