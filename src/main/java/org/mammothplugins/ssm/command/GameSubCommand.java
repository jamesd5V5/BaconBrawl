package org.mammothplugins.ssm.command;

import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.Game;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleSubCommand;

abstract class GameSubCommand extends SimpleSubCommand {

    protected GameSubCommand(String sublabel) {
        super(sublabel);
        setPermission("ssm.cmd");
        this.setPermissionMessage("&cYou don't have permission to use this command.");
    }

    protected final PlayerCache getCache() {
        return isPlayer() ? PlayerCache.from(getPlayer()) : null;
    }

    protected final Game findGameFromLocationOrFirstArg() {
        Game game;

        if (this.args.length > 0)
            game = this.findGame(args[0]); //this.joinArgs(0)

        else {
            game = Game.findByLocation(getPlayer().getLocation());

            this.checkNotNull(game, "Unable to locate a game. Type a game name. Available: " + Common.join(Game.getGameNames()));
        }

        return game;
    }

    protected final Game findGame(String name) {
        Game game = Game.findByName(name);
        this.checkNotNull(game, "No such game: '" + name + "'. Available: " + Common.join(Game.getGameNames()));

        return game;
    }

    protected final void checkGameExists(String gameName) {
        this.checkBoolean(Game.isGameLoaded(gameName),
                "No such game: '" + gameName + "'. Available: " + Common.join(Game.getGameNames()));
    }
}
