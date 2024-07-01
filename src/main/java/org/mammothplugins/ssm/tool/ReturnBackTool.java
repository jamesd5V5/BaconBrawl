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
public final class ReturnBackTool extends GameTool {

    @Getter
    private static final ReturnBackTool instance = new ReturnBackTool();

    @Override
    public ItemStack getItem() {
        return ItemCreator.of(CompMaterial.IRON_HOE,
                        "Return Back Location",
                        "",
                        "Click to set location",
                        "to return back players",
                        "when they join the server",
                        "and are in a game's region.")
                .glow(true)
                .make();
    }

    @Override
    protected CompMaterial getBlockMask(Block block, Player player) {
        return CompMaterial.BLACK_STAINED_GLASS;
    }

    @Override
    protected String getBlockName(Block block, Player player) {
        return "&8[&cReturn Back&8]";
    }

    @Override
    protected void onSuccessfulClick(Player player, Game game, Block block, ClickType click) {
        game.setReturnBackLocation(block.getLocation());

        Messenger.success(player, "Return back point set.");
    }

    @Override
    protected Location getGamePoint(Player player, Game game) {
        return game.getReturnBackLocation();
    }
}
