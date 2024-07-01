package org.mammothplugins.ssm.model.ssm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mammothplugins.ssm.model.GameScoreboard;
import org.mammothplugins.ssm.model.GameState;
import org.mineacademy.fo.model.Replacer;

import java.util.List;

public class SSMScoreBoard extends GameScoreboard { //gamePoint scoreboard
    public SSMScoreBoard(final SSMCore game) {
        super(game);
    }

    @Override
    protected String replaceVariables(final Player player, String message) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerCache playerCache = PlayerCache.from(player1);
            int plLives = playerCache.getLives();
            if (playerCache.hasGame()) {
                if (playerCache.getCurrentGame().getState() == GameState.PREPLAYED || playerCache.getCurrentGame().getState() == GameState.PLAYED)
                    message = Replacer.replaceArray(message,
                            "PlayerName:" + playerCache.getUniqueId(), getLifeColor(plLives) + playerCache.toPlayer().getName(),
                            "Lives:" + playerCache.getUniqueId(), plLives + " ");
            } else
                this.removeRow("PlayerName:" + playerCache.getUniqueId());
        }
        return super.replaceVariables(player, message);
    }

    @Override
    protected List<Object> onEditLines() {
        List<Object> list = super.onEditLines();

        return list;
    }

    @Override
    public void onGameStart() {
        super.onGameStart();

        int index = 4;
        for (PlayerCache cache : getGame().getPlayers(GameJoinMode.PLAYING)) {
            if (getRows().size() <= index)
                this.addRows("&f{Lives:" + cache.getUniqueId() + "}" + "&f{PlayerName:" + cache.getUniqueId() + "}");
            else
                this.setRow(index, "&f{Lives:" + cache.getUniqueId() + "}" + "&f{PlayerName:" + cache.getUniqueId() + "}");
            index++;
        }
    }

    private String getLifeColor(int lives) {
        String color = "";
        if (lives >= 4)
            color = "&a";
        else if (lives == 3)
            color = "&e";
        else if (lives == 2)
            color = "&6";
        else if (lives == 1)
            color = "&c";

        //Common.broadcast("Returned Color: " + color + "Test");
        return color;
    }

    @Override
    public SSMCore getGame() {
        return (SSMCore) super.getGame();
    }

    public void removePlayer(Player player) {
        this.removeRow("{PlayerName:" + PlayerCache.from(player).getUniqueId() + "}");
    }
}
