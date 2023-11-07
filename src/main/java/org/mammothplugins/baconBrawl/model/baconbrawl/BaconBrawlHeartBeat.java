package org.mammothplugins.baconBrawl.model.baconbrawl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.design.PlayerUIDesigns;
import org.mammothplugins.baconBrawl.model.GameHeartbeat;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.Kits;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers.Power;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.Random;

public class BaconBrawlHeartBeat extends GameHeartbeat {

    @Getter
    @Setter
    private ArrayList<Player> cooldown = new ArrayList<>();

    public BaconBrawlHeartBeat(final BaconBrawlCore game) {
        super(game);
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.startGameLogic();
    }

    @Override
    protected void onTick() {
        //ontick stuff
        for (PlayerCache cache : getGame().getPlayers(GameJoinMode.PLAYING)) {
            Player player = cache.toPlayer();

            //Display Cooldown
            ItemStack itemStack = player.getInventory().getItemInHand();
            for (Power power : cache.getCurrentKit().getPowers(player)) {
                if (power.getItemStack().equals(itemStack)) {
                    if (power.isCoolingDown() && power.canStartCooldown()) {
                        double timeLeft = power.getTimeLeftToCooldown();
                        Remain.sendActionBar(player, "&f&l" + power.getName() + "&r " +
                                PlayerUIDesigns.getLaunchBar(timeLeft, power.getCooldown()) +
                                "&r&f " + power.getConvertedTimeLeftCooldown() + " Seconds");
                    }
                }
            }

        }
    }

    private void startGameLogic() {
        for (PlayerCache cache : getGame().getPlayers(GameJoinMode.PLAYING)) {
            Player player = cache.toPlayer();
            player.getInventory().clear();
            player.setHealth(player.getMaxHealth());
            player.setSaturation(20);

            if (cache.isRandomKit()) {
                int num = new Random().nextInt(Kits.getKits().size());
                cache.setCurrentKit(Kits.getKits().get(num));
            }
            getGame().applyKit(player, cache);
        }
    }

    @Override
    public BaconBrawlCore getGame() {
        return (BaconBrawlCore) super.getGame();
    }
}
