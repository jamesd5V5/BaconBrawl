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

public class GameScoreboard extends SimpleScoreboard {

    @Getter
    private final Game game;

    public GameScoreboard(final Game game) {
        this.game = game;

        this.setTitle("&8---- &6&l" + game.getName() + " &8----");
        this.setTheme(ChatColor.GOLD, ChatColor.WHITE);
        this.setUpdateDelayTicks(20 /* 1 second */);
    }

    @Override
    protected String replaceVariables(final Player player, String message) {
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

    public void onPlayerJoin(final Player player) {
        this.show(player);
    }

    public void onPlayerLeave(final Player player) {
        if (this.isViewing(player))
            this.hide(player);
    }

    public void onLobbyStart() {
        this.addRows("",
                "Map: {gameName}",
                "Players: {players}/4",
                "",
                "Starting in: {remaining_start}",
                "State: {state}",
                "");
    }

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

    public void onGameStart() {
        for (int i = 6; i > 4; i--)
            this.getRows().remove(i);
    }

    public void onGameStop() {
        this.clearRows();

        this.stop();
    }

    public void onSpectateStart() {
    }

    public void removePlayer(Player player) {
        this.removeRow("{PlayerName:" + PlayerCache.from(player).getUniqueId() + "}");
    }
}
