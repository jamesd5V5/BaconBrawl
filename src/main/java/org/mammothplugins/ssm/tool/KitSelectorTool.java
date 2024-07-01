package org.mammothplugins.ssm.tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.menu.KitSelectorMenu;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompMaterial;

/**
 * Used to select what players to spectate
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KitSelectorTool extends Tool {

    @Getter
    private static final Tool instance = new KitSelectorTool();

    /**
     * @see Tool#getItem()
     */
    @Override
    public ItemStack getItem() {
        return ItemCreator.of(CompMaterial.GOLDEN_CARROT,
                        "&f&lKits",
                        "",
                        "Click to select",
                        "which kit you",
                        "want to play as.")
                .make();
    }

    /**
     * @see Tool#onBlockClick(PlayerInteractEvent)
     */
    @Override
    protected void onBlockClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final PlayerCache cache = PlayerCache.from(player);

        if (!cache.getCurrentGame().isLobby()) {
            Messenger.error(player, "You can only use this tool while in the lobby!");

            return;
        }

        KitSelectorMenu.openMenu(player);
    }

    @Override
    protected boolean ignoreCancelled() {
        return false;
    }

    /**
     * Automatically cancel the event
     */
    @Override
    protected boolean autoCancel() {
        return true;
    }
}
