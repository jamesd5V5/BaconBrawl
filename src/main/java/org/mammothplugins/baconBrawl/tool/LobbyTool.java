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
public final class LobbyTool extends GameTool {

    @Getter
    private static final LobbyTool instance = new LobbyTool();

    @Override
    public ItemStack getItem() {
        return ItemCreator.of(CompMaterial.IRON_SHOVEL,
                        "Lobby Tool",
                        "",
                        "&7Click to set game",
                        "&7lobby point.")
                .glow(true)
                .make();
    }

    @Override
    protected CompMaterial getBlockMask(Block block, Player player) {
        return CompMaterial.BLACK_STAINED_GLASS;
    }

    @Override
    protected String getBlockName(Block block, Player player) {
        return "&8[&fLobby Point&8]";
    }

    @Override
    protected void onSuccessfulClick(Player player, Game game, Block block, ClickType click) {
        game.setLobbyLocation(block.getLocation());

        Messenger.success(player, "Game lobby point set.");
    }

    @Override
    protected Location getGamePoint(Player player, Game game) {
        return game.getLobbyLocation();
    }
}
