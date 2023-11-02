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
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeathSpawn extends GameTool {

    @Getter
    private static final DeathSpawn instance = new DeathSpawn();

    @Override
    public ItemStack getItem() {
        return ItemCreator.of(CompMaterial.BONE,
                        "DeathSpawn Tool",
                        "",
                        "&7Click to set game",
                        "&7death spawnpoint.")
                .glow(true)
                .make();
    }

    @Override
    protected CompMaterial getBlockMask(Block block, Player player) {
        return CompMaterial.BLACK_STAINED_GLASS;
    }

    @Override
    protected String getBlockName(Block block, Player player) {
        return "&8[&fDeath Spawnpoint&8]";
    }

    @Override
    protected void onSuccessfulClick(Player player, Game game, Block block, ClickType click) {
        game.setDeathSpawnLocation(block.getLocation());

        Messenger.success(player, "Game death spawnpoint set.");
    }

    @Override
    protected Location getGamePoint(Player player, Game game) {
        return game.getLobbyLocation();
    }
}