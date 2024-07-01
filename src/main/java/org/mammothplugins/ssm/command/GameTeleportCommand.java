package org.mammothplugins.ssm.command;

import org.mammothplugins.ssm.model.Game;

import java.util.List;

final class GameTeleportCommand extends GameSubCommand {

    GameTeleportCommand() {
        super("teleport/tp");

        setDescription("Teleports to game lobby.");
        setUsage("<name>");
        setMinArguments(1);
        this.setPermission("ssm.cmd.admin.teleport");
    }

    @Override
    protected void onCommand() {
        this.checkConsole();

        final String name = this.joinArgs(0);
        this.checkGameExists(name);

        Game game = Game.findByName(name);
        this.checkNotNull(game.getLobbyLocation(), "This game does not have a lobby location set!");

        game.teleport(getPlayer(), game.getLobbyLocation());
        this.tellInfo("Teleporting to game '" + name + "'!");
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(Game.getGameNames()) : NO_COMPLETE;
    }
}
