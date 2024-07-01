package org.mammothplugins.ssm.tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.menu.SpectatePlayersMenu;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompMaterial;

/**
 * Used to select what players to spectate
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpectatePlayersTool extends Tool {

    @Getter
    private static final Tool instance = new SpectatePlayersTool();

    /**
     * @see Tool#getItem()
     */
    @Override
    public ItemStack getItem() {
        return ItemCreator.of(CompMaterial.COMPASS,
                        "&f&lSpectate players",
                        "",
                        "Click to select",
                        "what players you",
                        "want to spectate.")
                .make();
    }

    /**
     * @see Tool#onBlockClick(PlayerInteractEvent)
     */
    @Override
    protected void onBlockClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final PlayerCache cache = PlayerCache.from(player);

        if (cache.getCurrentGameMode() != GameJoinMode.SPECTATING) {
            Messenger.error(player, "You can only use this tool while spectating");

            return;
        }

        SpectatePlayersMenu.openMenu(player);
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
