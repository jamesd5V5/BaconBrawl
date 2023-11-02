package org.mammothplugins.baconBrawl.model.ssm;

import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mammothplugins.baconBrawl.model.GameSpawnPointScoreboard;
import org.mineacademy.fo.model.Replacer;

import java.util.*;

public class SSMScoreboard extends GameSpawnPointScoreboard {
    public SSMScoreboard(final SSM game) {
        super(game);
    }

    @Override
    protected String replaceVariables(final Player player, String message) {
//        List<PlayerCache> players = getGame().getPlayers(GameJoinMode.PLAYING);
//
//        ArrayList<Integer> lives = new ArrayList<>();
//        for (PlayerCache cache : players) {
//            lives.add(cache.getLives());
//        }
//
//        Collections.sort(lives, Collections.reverseOrder());
//        //How can i with this connect it to players
//
//        for (PlayerCache cache : players) {
//            int plLives = cache.getLives();
//            message = Replacer.replaceArray(message,
//                    "Lives:" + cache.getUniqueId(), plLives,
//                    "Life Color:" + cache.getUniqueId(), getLifeColor(plLives),
//                    "PlayerName:" + cache.getUniqueId(), cache.toPlayer().getName());
//        }
//        return super.replaceVariables(player, message);

//        List<PlayerCache> players = getGame().getPlayers(GameJoinMode.PLAYING);
//
//        Map<Integer, PlayerCache> livesMap = new HashMap<>();
//        for (PlayerCache cache : players) {
//            int plLives = cache.getLives();
//            livesMap.put(plLives, cache);
//        }
//
//        ArrayList<Integer> lives = new ArrayList<>(livesMap.keySet());
//        Collections.sort(lives, Collections.reverseOrder());

        for (PlayerCache cache : getGame().getPlayers(GameJoinMode.PLAYING)) {
            int plLives = cache.getLives();
            message = Replacer.replaceArray(message,
                    "Lives:" + cache.getUniqueId(), plLives,
                    "Life Color:" + cache.getUniqueId(), getLifeColor(plLives),
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
                this.addRows("&f{Lives:" + cache.getUniqueId() + "}" + " {Life Color:" + cache.getUniqueId() + "}" + "{PlayerName:" + cache.getUniqueId() + "}");
            else
                this.setRow(index, "&f{Lives:" + cache.getUniqueId() + "}" + " {Life Color:" + cache.getUniqueId() + "}" + "{PlayerName:" + cache.getUniqueId() + "}");
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

    private List<Integer> refreshScoreboard() {
        List<PlayerCache> players = getGame().getPlayers(GameJoinMode.PLAYING);

        // Create a map to associate player lives with PlayerCache objects
        Map<Integer, PlayerCache> livesMap = new HashMap<>();
        for (PlayerCache cache : players) {
            int plLives = cache.getLives();
            livesMap.put(plLives, cache);
        }

        // Sort the lives list in descending order
        ArrayList<Integer> lives = new ArrayList<>(livesMap.keySet());
        Collections.sort(lives, Collections.reverseOrder());
        return lives;
    }

    @Override
    public SSM getGame() {
        return (SSM) super.getGame();
    }
}
