package org.mammothplugins.baconBrawl.bungee;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mammothplugins.baconBrawl.settings.Settings;
import org.mineacademy.fo.BungeeUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.bungee.BungeeListener;
import org.mineacademy.fo.bungee.message.IncomingMessage;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

@AutoRegister
public final class GameBungeeListener extends BungeeListener {

    @Getter
    private static final GameBungeeListener instance = new GameBungeeListener();

    private GameBungeeListener() {
        super("plugin:lynx", LynxMessage.class);
    }

    public static void scheduleTask() {
        if (Settings.AutoMode.ENABLED) {
            List<? extends Game> games = Game.getGames();
            Valid.checkBoolean(games.size() == 1, "Bungee syncing requires 1 installed game, found " + games.size());

            Game game = games.get(0);
            Common.runTimerAsync(20, new SyncTask(game));
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class SyncTask implements Runnable {

        private final Game game;

        @Override
        public void run() {

            SerializedMap data = new SerializedMap();
            SerializedMap gameData = new SerializedMap();

            gameData.put("Game", this.game.getName());
            gameData.put("Players", this.game.getPlayers(GameJoinMode.PLAYING).size());
            gameData.put("CanPlay", this.game.isStopped() || (this.game.isLobby()
                    && this.game.getPlayersInAllModes().size() < this.game.getMaxPlayers()));
            gameData.put("CanSpectate", this.game.isPlayed());

            data.put(Remain.getServerName(), gameData);
            BungeeUtil.sendPluginMessage(LynxMessage.DATA_SYNC, data);
        }
    }

    @Override
    public void onMessageReceived(Player player, IncomingMessage message) {
        // Ignore
    }
}
