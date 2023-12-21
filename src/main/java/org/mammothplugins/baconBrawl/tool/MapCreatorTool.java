package org.mammothplugins.baconBrawl.tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.conversations.MapCreatorConversation;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompMaterial;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapCreatorTool extends Tool {

    private Player player;

    public MapCreatorTool(Player player) {
        this.player = player;
    }

    @Getter
    private static final Tool instance = new MapCreatorTool();

    @Override
    public ItemStack getItem() {
        return ItemCreator.of(CompMaterial.WRITTEN_BOOK,
                        "Map Creator: " + PlayerCache.from(player).getCurrentGame().getMapCreator(),
                        "",
                        "Click to notate",
                        "the player who",
                        "made the map.")
                .make();
    }

    @Override
    protected void onBlockClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final PlayerCache cache = PlayerCache.from(player);

        if (cache.getCurrentGameMode() != GameJoinMode.EDITING) {
            Messenger.error(player, "You can only use this tool while editing");

            return;
        }

        new MapCreatorConversation(player).start(player);
    }

    @Override
    protected boolean ignoreCancelled() {
        return false;
    }

    @Override
    protected boolean autoCancel() {
        return true;
    }
}
