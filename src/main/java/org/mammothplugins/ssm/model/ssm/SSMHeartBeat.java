package org.mammothplugins.ssm.model.ssm;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.design.PlayerUIDesigns;
import org.mammothplugins.ssm.model.GameHeartbeat;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mammothplugins.ssm.model.ssm.kits.Kits;
import org.mammothplugins.ssm.model.ssm.kits.powers.Power;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.Random;

public class SSMHeartBeat extends GameHeartbeat {

    @Getter
    @Setter
    private ArrayList<Player> cooldown = new ArrayList<>();

    public SSMHeartBeat(final SSMCore game) {
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
            setCompassTarget(player);

            //Display Cooldown
            ItemStack itemStack = player.getInventory().getItemInHand();
            for (Power power : cache.getCurrentKit().getPowers(player))
                if (power.getItemStack().getType().equals(itemStack.getType()))
                    if (power.isCoolingDown() && power.canStartCooldown()) {
                        double timeLeft = power.getTimeLeftToCooldown();
                        Remain.sendActionBar(player, "&f&l" + power.getName() + "&r " +
                                PlayerUIDesigns.getLaunchBar(timeLeft, power.getCooldown()) +
                                "&r&f " + power.getConvertedTimeLeftCooldown() + " Seconds");
                    }
        }
    }

    private void setCompassTarget(Player player) {
        Player target = getNearest(player, 50.0);
        if (target == null)
            return;
        player.setCompassTarget(target.getLocation());
    }

    public Player getNearest(Player player, Double range) {
        double distance = Double.POSITIVE_INFINITY;
        Player target = null;
        for (Entity e : player.getNearbyEntities(range, range, range)) {
            if (!(e instanceof Player))
                continue;
            for (PlayerCache cache : getGame().getPlayers(GameJoinMode.PLAYING)) {
                Player pl = cache.toPlayer();
                if (pl.getUniqueId().equals(e.getUniqueId())) {
                    double distanceto = player.getLocation().distance(e.getLocation());
                    if (distanceto > distance)
                        continue;
                    distance = distanceto;
                    target = (Player) e;
                }
            }
        }
        return target;
    }


    private void startGameLogic() {
        for (PlayerCache cache : getGame().getPlayers(GameJoinMode.PLAYING)) {
            Player player = cache.toPlayer();
            player.getInventory().clear();
            player.setHealth(player.getMaxHealth());

            player.setSaturation(20);

            getGame().joinMsg(player);
            cache.addGamesPlayed();


            if (cache.isRandomKit()) {
                boolean found = false;
                while (found == false) {
                    int num = new Random().nextInt(Kits.getKits().size());
                    Kits kit = Kits.getKits().get(num);
                    if (player.hasPermission("ssm.kits." + kit.getName())) {
                        cache.setCurrentKit(kit);
                        found = true;
                    }
                }
            }
            getGame().applyKit(player, cache);
        }
    }

    @Override
    public SSMCore getGame() {
        return (SSMCore) super.getGame();
    }
}
