package org.mammothplugins.baconBrawl.model;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.model.Replacer;
import org.mineacademy.fo.model.SimpleScoreboard;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple game scoreboard
 */
public class GameScoreboard extends SimpleScoreboard {

    /**
     * The game
     */
    @Getter
    private final Game game;

    /**
     * Create a new scoreboard
     *
     * @param game
     */
    public GameScoreboard(final Game game) {
        this.game = game;

        this.setTitle("&8---- &6&l" + "SSM" + " &8----");
        this.setTheme(ChatColor.GOLD, ChatColor.WHITE);
        this.setUpdateDelayTicks(20 /* 1 second */);
    }

    @Override
    protected String replaceVariables(final Player player, String message) {
        PlayerCache cache = PlayerCache.from(player);
        message = Replacer.replaceArray(message,
                "remaining_start", !this.game.getStartCountdown().isRunning() ? "Waiting"
                        : this.game.getStartCountdown().getTimeLeft() + "s",
                "player", player.getCustomName(),
                "gameName", game.getName(),

                "remaining_end", TimeUtil.formatTimeShort(this.game.getHeartbeat().getTimeLeft()), // -> 1m5s 1m4s
                "players", this.game.getPlayers(this.game.getState() == GameState.EDITED ? GameJoinMode.EDITING : GameJoinMode.PLAYING /* ignore spectators */).size(),
                "state", ItemUtil.bountifyCapitalized(this.game.getState()), // PLAYED -> Played
                "lobby_set", this.game.getLobbyLocation() != null,
                "region_set", this.game.getRegion().isWhole());

        return message.replace("true", "&ayes").replace("false", "&4no");
    }

    /**
     * Called automatically when the player joins
     *
     * @param player
     */
    public void onPlayerJoin(final Player player) {
        this.show(player);
    }

    /**
     * Called on player leave
     *
     * @param player
     */
    public void onPlayerLeave(final Player player) {
        if (this.isViewing(player))
            this.hide(player);
    }

    /**
     * Called automatically on lobby start
     */
    public void onLobbyStart() {
        this.addRows("",
                "Map: {gameName}",
                "Players: {players}/4",
                "",
                "Starting in: {remaining_start}",
                "State: {state}",
                "");
    }

    /**
     * Called automatically when the first player stars to edit the game
     */
    public final void onEditStart() {
        this.addRows("",
                "Editing players: {players}",
                "",
                "Lobby: {lobby_set}",
                "Region: {region_set}");

        this.addRows(this.onEditLines());
        this.addRows("",
                "&7Use: /game tools to edit.");
    }

    protected List<Object> onEditLines() {
        return new ArrayList<>();
    }

    /**
     * Called automatically when the game starts
     */
    public void onGameStart() {
        for (int i = 6; i > 4; i--)
            this.getRows().remove(i);
    }

    /**
     * Called on game stop
     */
    public void onGameStop() {
        this.clearRows();

        this.stop();
    }

    public void onSpectateStart() {
    }
}