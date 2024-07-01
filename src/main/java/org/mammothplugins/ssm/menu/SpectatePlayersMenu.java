package org.mammothplugins.ssm.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.List;

public class SpectatePlayersMenu extends MenuPagged<Player> {

    private SpectatePlayersMenu(Player viewer) {
        super(compilePlayers(viewer));

        setTitle("Select players to spectate");
    }

    private static List<Player> compilePlayers(Player viewer) {
        final PlayerCache cache = PlayerCache.from(viewer);
        Valid.checkBoolean(cache.getCurrentGameMode() == GameJoinMode.SPECTATING, "Spectate menu may only be opened in spectate arena mode!");

        return cache.getCurrentGame().getBukkitPlayers(GameJoinMode.PLAYING);
    }

    @Override
    protected ItemStack convertToItemStack(Player player) {
        return ItemCreator.of(
                        CompMaterial.PLAYER_HEAD,
                        player.getName(),
                        "",
                        "Click to teleport",
                        "to that player.")
                .skullOwner(player.getName())
                .make();
    }

    @Override
    protected void onPageClick(Player viewer, Player clickedPlayer, ClickType click) {
        viewer.closeInventory();
        viewer.teleport(clickedPlayer.getLocation().add(0, 1, 0));
        viewer.setCompassTarget(clickedPlayer.getLocation());

        Messenger.success(viewer, "You are now teleported to " + clickedPlayer.getName());
    }

    @Override
    protected String[] getInfo() {
        return new String[]{
                "Click a player to teleport",
                "to him to spectate his",
                "performance in the game!"
        };
    }

    public static void openMenu(Player player) {
        new SpectatePlayersMenu(player).displayTo(player);
    }

}