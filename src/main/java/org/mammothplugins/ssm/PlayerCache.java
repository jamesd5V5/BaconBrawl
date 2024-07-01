package org.mammothplugins.ssm;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mammothplugins.ssm.model.Game;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mammothplugins.ssm.model.ssm.kits.Creeper;
import org.mammothplugins.ssm.model.ssm.kits.Kits;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.YamlConfig;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class PlayerCache extends YamlConfig {

    private static volatile Map<UUID, PlayerCache> cacheMap = new HashMap<>();
    private final UUID uniqueId;
    private final String playerName;

    private int gamesPlayed;
    private int gamesWon;
    private int kills;
    private int currentKills;

    @Setter
    private Kits currentKit;
    @Setter
    private boolean randomKit = false;
    @Setter
    private Player potentialKiller;
    @Setter
    private boolean isCurrentlyBlocking;

    @Setter
    private GameJoinMode currentGameMode;

    @Setter
    private String currentGameName;

    @Setter
    private int lives;

    @Setter
    private boolean joining;

    @Setter
    private boolean leaving;

    private final StrictMap<String, Object> tags = new StrictMap<>();

    private PlayerCache(String name, UUID uniqueId) {
        this.playerName = name;
        this.uniqueId = uniqueId;

        this.setPathPrefix("Players." + uniqueId.toString());
        this.loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
    }

    @Override
    protected void onLoad() {
        this.gamesPlayed = getInteger("GamesPlayed", 0);
        this.gamesWon = getInteger("GamesWon", 0);
        this.kills = getInteger("Kills", 0);
        this.currentKit = Kits.getKit(getString("Kit", "Creeper"));
        this.randomKit = getBoolean("RandomKit", false);
    }

    @Override
    public void onSave() {
        this.set("GamesPlayed", this.gamesPlayed);
        this.set("GamesWon", this.gamesWon);
        this.set("Kills", this.kills);
        this.set("Kit", this.currentKit.getName());
        this.set("RandomKit", this.randomKit);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlayerCache && ((PlayerCache) obj).getUniqueId().equals(this.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.uniqueId);
    }

    public void setFreshKit(String kitName) { //Todo May cause Overload of Memory
        String packageName = "org.mammothplugins.ssm.model.ssm.kits.";
        String className = packageName + kitName.replace(" ", "");
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            setCurrentKit((Kits) clazz.getDeclaredConstructor().newInstance());
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Kits getCurrentKit() {
        Kits creeper = new Creeper();
        if (currentKit == null)
            setCurrentKit(creeper);
        return currentKit;
    }

    public boolean hasKit(Kits kits) {
        if (currentKit.getName().equals(kits.getName()))
            return true;
        return false;
    }

    public void addGamesPlayed() {
        this.gamesPlayed++;
    }

    public void addGamesWon() {
        this.gamesWon++;
    }

    public void addCurrentKills() {
        this.currentKills++;
        this.kills++;
    }

    public void resetCurrentKills() {
        this.currentKills = 0;
    }

    public Game getCurrentGame() {
        if (this.hasGame()) {
            final Game game = Game.findByName(this.currentGameName);
            Valid.checkNotNull(game, "Found player " + this.playerName + " having unloaded game " + this.currentGameName);

            return game;
        }

        return null;
    }

    public boolean hasGame() {

        // Integrity check
        if ((this.currentGameName != null && this.currentGameMode == null) || (this.currentGameName == null && this.currentGameMode != null))
            throw new FoException("Current game and current game mode must both be set or both be null, " + this.getPlayerName() + " had game " + this.currentGameName + " and mode " + this.currentGameMode);

        return this.currentGameName != null;
    }

    public boolean hasPlayerTag(final String key) {
        return getPlayerTag(key) != null;
    }

    public <T> T getPlayerTag(final String key) {
        final Object value = this.tags.get(key);

        return value != null ? (T) value : null;
    }

    public void setPlayerTag(final String key, final Object value) {
        this.tags.override(key, value);
    }

    public void removePlayerTag(final String key) {
        this.tags.remove(key);
    }

    public void clearTags() {
        this.tags.clear();
    }

    /* ------------------------------------------------------------------------------- */
    /* Misc methods */
    /* ------------------------------------------------------------------------------- */

    @Nullable
    public Player toPlayer() {
        final Player player = Remain.getPlayerByUUID(this.uniqueId);

        return player != null && player.isOnline() ? player : null;
    }

    public void removeFromMemory() {
        synchronized (cacheMap) {
            cacheMap.remove(this.uniqueId);
        }
    }

    @Override
    public String toString() {
        return "PlayerCache{" + this.playerName + ", " + this.uniqueId + "}";
    }

    private boolean canstop;
    private boolean samePerson = false;
    BukkitTask countdownTask1;
    BukkitTask countdownTask2;

    public void startCountdownLastKiller(Player damager) {
        Player victim = toPlayer();

        canstop = false;

        PlayerCache vCache = PlayerCache.from((Player) victim);
        Game game = vCache.getCurrentGame();

        if (game.getLastHit().get(victim.getUniqueId()) == null) {
            samePerson = false;
            game.getLastHit().put(victim.getUniqueId(), damager.getUniqueId());
        } else {
            if (game.getLastHit().get(victim.getUniqueId()).equals(damager.getUniqueId())) {
                samePerson = true;
                if (countdownTask1 != null)
                    countdownTask1.cancel();
                if (countdownTask2 != null)
                    countdownTask2.cancel();

            } else {
                samePerson = false;
                game.getLastHit().put(victim.getUniqueId(), damager.getUniqueId());
            }
        }

        countdownTask1 = new BukkitRunnable() {
            @Override
            public void run() {
                if (damager.isDead() || PlayerCache.from(damager).getCurrentGame() == null ||
                        victim.isDead() || PlayerCache.from((Player) victim).getCurrentGame() == null) {
                    canstop = true;
                    cancel();
                    return;
                }
                if (game.getLastHit().get(victim.getUniqueId()) == null || !(game.getLastHit().get(victim.getUniqueId()).equals(damager.getUniqueId()))) { // || !(game.getLastHit().get(victim.getUniqueId()).equals(damager.getUniqueId()))
                    canstop = true;
                    //Common.broadcast("Something interrupted the last hit of power.");
                    cancel();
                    return;
                }

            }
        }.runTaskTimer(SSM.getInstance(), 0L, 0L);
        countdownTask2 = new BukkitRunnable() {
            @Override
            public void run() {
                if (canstop == true) {
                    canstop = false;
                    return;
                } else {
                    Common.broadcast("Removed LastHit: " + victim.getUniqueId());
                    //Common.broadcast("Just Cleared Killer");
                    vCache.getCurrentGame().getLastHit().put(victim.getUniqueId(), null);
                }
            }
        }.runTaskLater(SSM.getInstance(), 5 * 20L);
        samePerson = false;
    }

    /* ------------------------------------------------------------------------------- */
    /* Static access */
    /* ------------------------------------------------------------------------------- */

    public static PlayerCache from(Player player) {
        synchronized (cacheMap) {
            final UUID uniqueId = player.getUniqueId();
            final String playerName = player.getName();

            PlayerCache cache = cacheMap.get(uniqueId);

            if (cache == null) {
                cache = new PlayerCache(playerName, uniqueId);

                cacheMap.put(uniqueId, cache);
            }

            return cache;
        }
    }

    public static void clearCaches() {
        synchronized (cacheMap) {
            cacheMap.clear();
        }
    }
}
