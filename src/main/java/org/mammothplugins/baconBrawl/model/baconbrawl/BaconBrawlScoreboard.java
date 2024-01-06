package org.mammothplugins.baconBrawl.model.baconbrawl;

import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mammothplugins.baconBrawl.model.GameScoreboard;
import org.mineacademy.fo.model.Replacer;

import java.util.List;

public class BaconBrawlScoreboard extends GameScoreboard { //gamePoint scoreboard
    public BaconBrawlScoreboard(final BaconBrawlCore game) {
        super(game);
    }

    @Override
    protected String replaceVariables(final Player player, String message) {
        for (PlayerCache cache : getGame().getPlayers(GameJoinMode.PLAYING)) {
            message = Replacer.replaceArray(message,
                    "PlayerName:" + cache.getUniqueId(), cache.toPlayer().getName());
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
                this.addRows("&f{PlayerName:" + cache.getUniqueId() + "}");
            else
                this.setRow(index, "&f{PlayerName:" + cache.getUniqueId() + "}");
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
        return color;
    }

    @Override
    public BaconBrawlCore getGame() {
        return (BaconBrawlCore) super.getGame();
    }

    public void removePlayer(Player player) {
        this.removeRow("{PlayerName:" + PlayerCache.from(player).getUniqueId() + "}");
    }
}
