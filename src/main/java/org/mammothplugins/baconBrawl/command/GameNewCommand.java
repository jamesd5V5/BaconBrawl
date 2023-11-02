package org.mammothplugins.baconBrawl.command;

import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mammothplugins.baconBrawl.model.GameType;
import org.mineacademy.fo.settings.SimpleSettings;

import java.util.List;

final class GameNewCommand extends GameSubCommand {

    GameNewCommand() {
        super("new");

        setDescription("Creates a new game.");
        setUsage("<gameType> <name>");
        setMinArguments(2);

        // /game new mobarena My Awesome Mob Arena
    }

    @Override
    protected void onCommand() {
        final GameType type = this.findEnum(GameType.class, args[0], "No such game type '{0}'. Available: {available}");
        final String name = this.joinArgs(1);
        this.checkBoolean(!Game.isGameLoaded(name), "Game: '" + name + "' already exists!");

        Game game = Game.createGame(name, type);

        if (isPlayer()) {
            game.joinPlayer(getPlayer(), GameJoinMode.EDITING);

            getPlayer().performCommand(SimpleSettings.MAIN_COMMAND_ALIASES.get(0) + " tools");
        }

        tellSuccess("Created " + type.getName() + " game '" + name + "'!");
    }

    @Override
    protected List<String> tabComplete() {
        return this.args.length == 1 ? this.completeLastWord(GameType.values()) : NO_COMPLETE;
    }
}
