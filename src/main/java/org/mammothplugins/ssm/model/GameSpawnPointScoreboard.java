package org.mammothplugins.ssm.model;

import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.model.Replacer;

import java.util.List;

public class GameSpawnPointScoreboard extends GameScoreboard {

    public GameSpawnPointScoreboard(final GameSpawnPoint game) {
        super(game);
    }

    @Override
    protected String replaceVariables(final Player player, String message) {
        final int playerSpawnpoints = this.getGame().getPlayerSpawnpoints().size();

        final int maxPlayers = this.getGame().getMaxPlayers();

        message = Replacer.replaceArray(message,
                "player_spawnpoints", (playerSpawnpoints >= maxPlayers ? "&a" : "") + playerSpawnpoints + "/" + maxPlayers);

        return super.replaceVariables(player, message);
    }

    @Override
    protected List<Object> onEditLines() {
        return Common.newList("Player spawnpoints: {player_spawnpoints}");
    }

    @Override
    public GameSpawnPoint getGame() {
        return (GameSpawnPoint) super.getGame();
    }
}
