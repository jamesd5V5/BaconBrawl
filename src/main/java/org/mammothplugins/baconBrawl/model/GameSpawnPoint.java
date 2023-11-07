package org.mammothplugins.baconBrawl.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.RandomNoRepeatPicker;

public abstract class GameSpawnPoint extends Game {

    private final RandomNoRepeatPicker<Location> playerSpawnpointPicker = RandomNoRepeatPicker.newPicker(Location.class);

    private LocationList playerSpawnpoints;

    protected GameSpawnPoint(String name) {
        super(name);
    }

    protected GameSpawnPoint(String name, @Nullable GameType type) {
        super(name, type);
    }

    @Override
    protected void onLoad() {
        this.playerSpawnpoints = this.getLocationList("Player_Spawnpoint");

        super.onLoad();
    }

    @Override
    protected void onSave() {
        super.onSave();

        this.set("Player_Spawnpoint", this.playerSpawnpoints);
    }

    public final LocationList getPlayerSpawnpoints() {
        return this.playerSpawnpoints;
    }

    protected final RandomNoRepeatPicker<Location> getPlayerSpawnpointPicker() {
        return playerSpawnpointPicker;
    }

    @Override
    protected void onGameStart() {
        super.onGameStart();

        this.playerSpawnpointPicker.setItems(this.playerSpawnpoints.getLocations());
    }

    @Override
    protected void onGameStartFor(Player player, PlayerCache cache) {
        Location spawnpoint = this.playerSpawnpointPicker.pickRandom();

        if (spawnpoint != null) {
            this.teleport(player, spawnpoint);

            cache.setPlayerTag("Spawnpoint", spawnpoint);
        } else {
            Common.runLater(() -> {
                this.leavePlayer(player, GameLeaveReason.ERROR);

                throw new FoException("Unable to pick spawnpoint for " + player.getName() + ", leaving him from " + this.getName());
            });
        }
    }

    @Override
    public final Location getRespawnLocation(Player player) {
        return PlayerCache.from(player).getPlayerTag("Spawnpoint");
    }

    @Override
    public final boolean isSetup() {
        return super.isSetup() && this.areSpawnPointsSetup();
    }

    protected boolean areSpawnPointsSetup() {
        return this.playerSpawnpoints.size() >= this.getMaxPlayers();
    }
}
