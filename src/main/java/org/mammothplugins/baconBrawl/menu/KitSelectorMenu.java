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

import java.util.Arrays;

public class KitSelectorMenu extends MenuPagged<Kits> {

    private Player player;
    private final Button randomButton;
    private final Button creditButton;
    private final Button statsButton;

    public KitSelectorMenu(Player player) {
        super(Kits.getKits());
        this.setTitle("Bacon Brawl Kits");
        this.player = player;
        this.setSize(9 * 2);
        PlayerCache cache = PlayerCache.from(player);
        this.randomButton = new Button() {
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                if (clickType.isLeftClick()) {

                    if (player.hasPermission("baconbrawl.kits.random")) {
                        if (cache.isRandomKit()) {
                            cache.setRandomKit(false);
                            Common.tell(player, "&7You selected &a&l" + cache.getCurrentKit().getName() + " Kit&7!");
                            animateTitle("&a&lRandom kit Enabled");
                        } else {
                            cache.setRandomKit(true);
                            Common.tell(player, "&7You selected &a&l" + "Random" + " Kit&7!");
                            animateTitle("&c&lRandom kit Disabled");
                        }

                        CompSound.NOTE_PIANO.play(player);
                        (new KitSelectorMenu(player)).displayTo(player);
                    } else {
                        CompSound.VILLAGER_NO.play(player);
                        animateTitle("&c&lNo Permission!");
                    }
                }
            }

            public ItemStack getItem() {
                return ItemCreator.of(cache.isRandomKit() ? CompMaterial.LIME_DYE : CompMaterial.RED_DYE, "&f&lRandom Kit", new String[]{"&7Randomly assigns a kit", "at the start of every game.", " ", cache.isRandomKit() ? "&f(Click to &c&lDisable&r&f)" : "&f(Click to &a&lEnable&r&f)"}).glow(cache.isRandomKit()).make();
            }

        };

        this.creditButton = new Button() {
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                if (clickType.isLeftClick()) {
                }
            }

            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.PLAYER_HEAD, "&f&lMammoth Plugins", new String[]{
                        "&7In honor one of Mineplex's best games\n" + "Recreated by jamesd5."}).skullOwner("jamesd5").make();
            }

        };

        this.statsButton = new Button() {
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                if (clickType.isLeftClick()) {
                }
            }

            public ItemStack getItem() {
                double num = (double) cache.getGamesWon() / cache.getGamesPlayed();
                String numb = String.format("%.2f", num);
                String ratio = "" + numb;

                double other = (double) cache.getKills() / cache.getGamesPlayed();
                String numbb = String.format("%.2f", other);
                String killRatio = "" + numbb;

                return ItemCreator.of(CompMaterial.PLAYER_HEAD, "&f&l" + player.getName() + "'s Stats", new String[]{
                        " \n",
                        "&7Games Played: &f" + cache.getGamesPlayed() + "\n",
                        "&7Games Won: &f" + cache.getGamesWon() + "\n",
                        "&7W/L Ratio: &f" + ratio + "\n",
                        " \n",
                        "&7Kills: &f" + cache.getKills() + "\n",
                        "&7Kills/Game Ratio: &f" + killRatio + "\n",
                }).skullOwner(player.getName()).make();
            }

        };
    }

    @Override
    public ItemStack getItemAt(int slot) {
        if (slot == this.getSize() - 5)
            return this.randomButton.getItem();
        if (slot == this.getSize() - 1)
            return this.creditButton.getItem();
        if (slot == this.getSize() - 2)
            return this.statsButton.getItem();
        else if (slot >= this.getSize() - 9)
            return ItemCreator.of(CompMaterial.GRAY_STAINED_GLASS_PANE, " ", new String[0]).make();

        return super.getItemAt(slot);
    }

    protected ItemStack convertToItemStack(Kits kit) {
        PlayerCache cache = PlayerCache.from(player);
        Kits currentKit = cache.getCurrentKit();
        return ItemCreator.of(kit.getCompMaterial(), "&f&l" + kit.getName() + " Kit", "&7" + String.join("\n", Arrays.asList(kit.getLore())), "", "&f(Click to Select)").glow(!cache.isRandomKit() && currentKit.getName().equals(kit.getName())).make();
    }

    protected void onPageClick(Player player, Kits kit, ClickType clickType) {
        if (clickType.isLeftClick()) {
            if (player.hasPermission("baconbrawl.kits." + kit.getName())) {
                PlayerCache cache = PlayerCache.from(player);
                cache.setRandomKit(false);
                cache.setCurrentKit(kit);
                CompSound.NOTE_PIANO.play(player);
                Common.tell(player, "&7You selected &a&l" + kit.getName() + " Kit&7!");

                Menu menu = (new KitSelectorMenu(player));
                menu.displayTo(player);

                menu.animateTitle("Selected " + kit.getName());
                animateTitle("Selected " + kit.getName());

            } else {
                CompSound.VILLAGER_NO.play(player);
                animateTitle("&c&lNo permission!");
            }
        }
    }

    public static void openMenu(Player player) {
        new KitSelectorMenu(player).displayTo(player);
    }
}