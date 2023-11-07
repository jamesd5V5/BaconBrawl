package org.mammothplugins.baconBrawl.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.Kits;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;

public class KitSelectorMenu extends MenuPagged<Kits> {

    private Player player;
    private final Button randomButton;

    public KitSelectorMenu(Player player) {
        super(Kits.getKits());
        this.setTitle("&3&lSelect a Kit");
        this.player = player;
        this.setSize(9 * 2);
        PlayerCache cache = PlayerCache.from(player);
        this.randomButton = new Button() {
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                if (clickType.isLeftClick()) {

                    if (cache.isRandomKit()) {
                        cache.setRandomKit(false);
                        Common.tell(player, "&7You selected &a&l" + cache.getCurrentKit().getName() + " Kit&7!");
                    } else {
                        cache.setRandomKit(true);
                        Common.tell(player, "&7You selected &a&l" + "Random" + " Kit&7!");
                    }

                    CompSound.NOTE_PIANO.play(player);
                    (new KitSelectorMenu(player)).displayTo(player);
                }
            }

            public ItemStack getItem() {
                return ItemCreator.of(cache.isRandomKit() ? CompMaterial.LIME_DYE : CompMaterial.RED_DYE, "&3&lRandom Kit", new String[]{" ", cache.isRandomKit() ? "&7Click to Disable" : "Click to Enable"}).glow(cache.isRandomKit()).make();
            }
        };
    }

    @Override
    public ItemStack getItemAt(int slot) {
        if (slot == this.getSize() - 5)
            return this.randomButton.getItem();
        else if (slot >= this.getSize() - 9)
            return ItemCreator.of(CompMaterial.GRAY_STAINED_GLASS_PANE, " ", new String[0]).make();

        return super.getItemAt(slot);
    }

    protected ItemStack convertToItemStack(Kits kit) {
        PlayerCache cache = PlayerCache.from(player);
        Kits currentKit = cache.getCurrentKit();
        return ItemCreator.of(kit.getCompMaterial(), "&f&l" + kit.getName() + " Kit").glow(!cache.isRandomKit() && currentKit.getName().equals(kit.getName())).make();
    }

    protected void onPageClick(Player player, Kits kit, ClickType clickType) {
        if (clickType.isLeftClick()) {
            PlayerCache cache = PlayerCache.from(player);
            cache.setRandomKit(false);
            cache.setCurrentKit(kit);
            Common.tell(player, "&7You selected &a&l" + kit.getName() + " Kit&7!");
            CompSound.NOTE_PIANO.play(player);
            (new KitSelectorMenu(player)).displayTo(player);
        }
    }

    public static void openMenu(Player player) {
        new KitSelectorMenu(player).displayTo(player);
    }
}