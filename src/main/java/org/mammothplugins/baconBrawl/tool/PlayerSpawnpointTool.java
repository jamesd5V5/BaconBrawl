package org.mammothplugins.baconBrawl.tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.baconBrawl.model.Game;
import org.mammothplugins.baconBrawl.model.GameSpawnPoint;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.FileConfig.LocationList;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerSpawnpointTool extends GameTool {

    @Getter
    private static final PlayerSpawnpointTool instance = new PlayerSpawnpointTool();

    @Override
    public ItemStack getItem() {
        return ItemCreator.of(CompMaterial.IRON_SWORD,
                        "Player Spawnpoint",
                        "",
                        "Click block to set",
                        "player spawnpoint")
                .glow(true)
                .make();
    }

    @Override
    protected CompMaterial getBlockMask(Block block, Player player) {
        return CompMaterial.BROWN_STAINED_GLASS;
    }

    @Override
    protected String getBlockName(Block block, Player player) {
        return "&8[&e Player Spawnpoint&8]";
    }

    @Override
    protected void onSuccessfulClick(Player player, Game game, Block block, ClickType click) {
        if (!(game instanceof GameSpawnPoint)) {
            Messenger.error(player, "You can only use this tool for games with player spawnpoints.");

            return;
        }
        if (game.getRegion().isWhole() == false)
            Messenger.error(player, "Please set the game's region first.");
        else if (game.getRegion().isWithin(block.getLocation())) {
            final GameSpawnPoint spawnpointGame = (GameSpawnPoint) game;
            final LocationList points = spawnpointGame.getPlayerSpawnpoints();
            final int maxLimit = game.getMaxPlayers();

            if (points.size() >= maxLimit && !points.hasLocation(block.getLocation())) {
                Messenger.error(player, "Cannot place more points than game max players (" + maxLimit + ").");

                return;
            }

            final boolean added = points.toggle(block.getLocation());
            Messenger.success(player, "Player spawnpoint was " + (added ? "added" : "removed") + " (" + points.size()
                    + "/" + maxLimit + ").");
        } else {
            Messenger.error(player, "Must be set within the game's region.");
        }
    }

    @Override
    protected List<Location> getGamePoints(Player player, Game game) {
        return game instanceof GameSpawnPoint ? ((GameSpawnPoint) game).getPlayerSpawnpoints().getLocations() : null;
    }
}