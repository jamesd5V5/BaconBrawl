package org.mammothplugins.baconBrawl.model.baconbrawl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.design.PlayerUIDesigns;
import org.mammothplugins.baconBrawl.model.GameHeartbeat;
import org.mammothplugins.baconBrawl.model.GameJoinMode;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.Kits;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers.Power;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.HashMap;
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1, 100));
            setCompassTarget(player);

            //Display Cooldown
            ItemStack itemStack = player.getInventory().getItemInHand();
            for (Power power : cache.getCurrentKit().getPowers(player)) {
                //Common.tell(player, "Wow powerL " + power.getItemStack().getType().toString() + itemStack.getType().toString());
                if (power.getItemStack().getType().equals(itemStack.getType())) {
                    //Common.tell(player, "You are being told IsCooling: " + power.isCoolingDown() + " canstartCooling: " + power.canStartCooldown());
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

    private void setCompassTarget(Player player) {
        Location playerLoc = player.getLocation();
        HashMap<Player, Double> distance = new HashMap<>();
        for (Entity e : player.getNearbyEntities(50, 50, 50)) {
            if (e instanceof Player) {
                Player other = (Player) e;

                if (getGame().getPlayers(GameJoinMode.PLAYING).contains(other)) {
                    Common.tell(player, "Heyyyyy");
                    double diff = other.getLocation().distance(playerLoc);
                    distance.put(other, diff);
                }
            }
        }
        double closestDistance = -1;
        for (Player player1 : distance.keySet()) {

            if (player1 == null || !getGame().getPlayers(GameJoinMode.PLAYING).contains(player1))
                distance.remove(player1);
            else {
                if (distance.get(player1) < closestDistance || closestDistance == -1) {
                    closestDistance = distance.get(player1);
                    player.setCompassTarget(player1.getLocation());
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
