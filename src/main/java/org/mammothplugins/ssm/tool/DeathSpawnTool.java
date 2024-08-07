package org.mammothplugins.ssm.tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.ssm.model.Game;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeathSpawnTool extends GameTool {

    @Getter
    private static final DeathSpawnTool instance = new DeathSpawnTool();

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
        if (game.getRegion().isWhole() == false)
            Messenger.error(player, "Please set the game's region first.");
        else if (game.getRegion().isWithin(block.getLocation())) {
            game.setDeathSpawnLocation(block.getLocation());
            Messenger.success(player, "Game death spawnpoint set.");
        } else {
            Messenger.error(player, "Must be set within the game's region.");
        }
    }

    @Override
    protected Location getGamePoint(Player player, Game game) {
        return game.getDeathSpawnLocation();
    }
}
