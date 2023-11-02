package org.mammothplugins.baconBrawl.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class GameKitPvP extends Game {

    private GameKitPvP(String name) {
        super(name);
    }

    private GameKitPvP(String name, @Nullable GameType type) {
        super(name, type);
    }

    @Override
    public Location getRespawnLocation(Player player) {
        return null;
    }
}
