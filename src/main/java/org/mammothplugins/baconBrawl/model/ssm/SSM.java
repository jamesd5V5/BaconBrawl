package org.mammothplugins.baconBrawl.model.ssm;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.mammothplugins.baconBrawl.BaconBrawl;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.*;
import org.mammothplugins.baconBrawl.model.ssm.kits.Kits;
import org.mammothplugins.baconBrawl.model.ssm.kits.nms.NmsDisguise;
import org.mammothplugins.baconBrawl.model.ssm.kits.powers.Power;
import org.mammothplugins.baconBrawl.tool.KitSelectorTool;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.model.BoxedMessage;
import org.mineacademy.fo.remain.*;

import java.util.*;

public final class SSM extends GameSpawnPoint {

    private Player[] winners = new Player[3];

    protected SSM(String name) {
        super(name);
    }

    protected SSM(String name, @Nullable GameType type) {
        super(name, type);
    }

    @Override
    protected GameHeartbeat compileHeartbeat() {
        return new SSMHeartBeat(this);
    }

    @Override
    protected GameScoreboard compileScoreboard() {
        return new SSMScoreboard(this);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
    }

    @Override
    protected void onSave() {
        super.onSave();
    }

    @Override
    public void onPlayerJump(PlayerMoveEvent event) {
    }

    public ArrayList<Player> getCooldowns() {
        SSMHeartBeat SSMHeartBeat = (SSMHeartBeat) getHeartbeat();
        return SSMHeartBeat.getCooldown();
    }


    @Override
    public void onEntityClick(Player player, Entity entity, PlayerInteractEntityEvent event) {
        super.onEntityClick(player, entity, event);

        if (entity instanceof Creeper)
            ((Creeper) entity).ignite(); // DOES NOT WORK ON 1.8.8
    }

    public void startDeathSpectate(Player player) {
        PlayerCache cache = PlayerCache.from(player);
        Common.tell(player, "&c&lYou have died!");
        Common.tell(player, "&c&lYou have " + cache.getLives() + " lives left!");


        cache.setPlayerTag("InSpectateDeath", true);
        cache.getCurrentKit().clearItems(player);
        if (cache.isRandomKit()) {
            int num = new Random().nextInt(Kits.getKits().size());
            cache.setCurrentKit(Kits.getKits().get(num));
        }

        transformToDeathSpectate(player);
        final int[] count = {4};

        Common.runLater(2, () -> {
            new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 1).apply(player);
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                if (count[0] <= 0) {
                    cancel();
                    return;
                }
                Remain.sendTitle(player, 0, 1 * 21, 0, "&c&lYOU DIED!", "&fRespawining in &c" + count[0] + " &fseconds...");
                count[0]--;
            }
        }.runTaskTimer(BaconBrawl.getInstance(), 0L, 20L);
        Common.runLater(4 * 20 + 1, () -> {
            cache.getCurrentKit().applyAttributes(player);
            Common.tell(player, "&7You respawned as a " + cache.getCurrentKit().getName() + ".");

            cache.removePlayerTag("InSpectateDeath");
            untransfromToDeathSpectate(player);
        });
    }

    @Override
    public void onPlayerDeath(PlayerCache cache, PlayerDeathEvent event) {
        super.onPlayerDeath(cache, event);
        Player player = event.getEntity();

        cache.setLives(cache.getLives() - 1);
        cache.getCurrentKit().onDeath(player);
        for (Power power : PlayerCache.from(player).getCurrentKit().getPowers(player))
            power.resetPower();
        //PlayerUIDesigns.deathMessage(cache.getCurrentGame(), event);

        // No Respawn Screen
        Remain.respawn(player);

        int currentLives = cache.getLives();
        if (currentLives <= 0) {
            int remainingPlayers = getPlayers(GameJoinMode.PLAYING).size();

            if (remainingPlayers == 3)
                winners[2] = player; //3rd Place Winner
            if (remainingPlayers <= 2) {
                winners[1] = player; //2nd Place Winner
                for (PlayerCache playerCache : getPlayers(GameJoinMode.PLAYING))
                    if (!(playerCache.getUniqueId().equals(player.getUniqueId())))
                        winners[0] = playerCache.toPlayer(); //1st Place Winner

                onGameStop();

                Common.runLater(2, () -> {
                    onGameStopMessage(GameStopReason.LAST_PLAYER_LEFT);
                    stop(GameStopReason.LAST_PLAYER_LEFT);
                    Arrays.fill(winners, null); //resets winners Array
                });
                return;
            }
            cache.setCurrentGameMode(GameJoinMode.SPECTATING);
        } else {
            startDeathSpectate(player);
        }
    }

    private Set<Player> doubleJumpers = new HashSet<>();

    @Override
    public void onPlayerDoubleJump(Player player, PlayerToggleFlightEvent event) {
        PlayerCache cache = PlayerCache.from(player);
        if (cache.getCurrentGame().isLobby())
            return;
        if (cache.getCurrentGameMode() == GameJoinMode.SPECTATING || cache.getCurrentGameMode() == GameJoinMode.EDITING)
            return;
        if (cache.hasPlayerTag("InSpectateDeath"))
            return;
        super.onPlayerDoubleJump(player, event);

        if (player.isOnGround() || player.isFlying()) {
            return;
        }

        if (event.isFlying() && !doubleJumpers.contains(player)) {
            event.setCancelled(true); // Prevent normal flight
            doubleJumpers.add(player);

            CompSound.FIREWORK_BLAST.play(player, 0.5f, 1f);
            for (int i = 0; i < 20; i++)
                CompParticle.SMOKE_NORMAL.spawn(RandomUtil.nextLocation(player.getLocation(), 1, true));

            Vector playerDirection = player.getLocation().getDirection();

            if (playerDirection.getY() <= -0.9) {
                // Looking down, jump opposite direction
                Vector oppositeDirection = playerDirection.multiply(-1);
                Vector jumpVector = oppositeDirection.multiply(0.9).add(new Vector(0, 0.10, 0));
                player.setVelocity(jumpVector);
            } else {
                // Looking forward or up, jump in the same direction
                Vector jumpVector = playerDirection.multiply(0.9).add(new Vector(0, 0.5, 0));
                player.setVelocity(jumpVector);
            }
        }
    }

    @Override
    public void onPlayerLand(Player player, PlayerMoveEvent event) {
        PlayerCache cache = PlayerCache.from(player);
        if (cache.getCurrentGame().isLobby())
            return;
        if (cache.getCurrentGameMode() == GameJoinMode.SPECTATING || cache.getCurrentGameMode() == GameJoinMode.EDITING)
            return;
//        if (!cache.getCurrentKit().getName().equals("Creeper"))
//            return;
        super.onPlayerLand(player, event);
        if (player.isOnGround()) {
            doubleJumpers.remove(player);
            player.setAllowFlight(true);
        }
    }

    public void applyKit(Player player, PlayerCache cache) {
        Kits kit = cache.getCurrentKit();
        kit.applyAttributes(player);


        Common.tell(player, Common.colorize("&7You equipped " + kit.getChatColor() + kit.getName() + " Kit"));
    }

    @Override
    protected void onGameJoin(Player player, GameJoinMode mode) {
        super.onGameJoin(player, mode);

        KitSelectorTool.getInstance().give(player, 4);
    }

    @Override
    protected void onGameStartFor(Player player, PlayerCache cache) {
        super.onGameStartFor(player, cache);

        cache.setLives(4);
    }

    @Override

    protected void onGameStop() {
        super.onGameStop();

        for (PlayerCache cache : getPlayers(GameJoinMode.PLAYING)) {
            cache.getCurrentKit().wipeAllPowers();
            cache.getCurrentKit().onDeath(cache.toPlayer());
            Common.runLater(2, () -> {
                NmsDisguise.removeDisguise(cache.toPlayer());
            });
        }
    }

    public void onGameStopMessage(GameStopReason stopReason) {
        if (stopReason == GameStopReason.LAST_PLAYER_LEFT)
            this.forEachPlayerInAllModes(player -> {
                leaveMsg(player);
            });

    }

    @Override
    protected void onGameLeave(Player player) {
        super.onGameLeave(player);

        if (this.isPlayed()) {
            Common.runLater(2, () -> {
                NmsDisguise.removeDisguise(player);
                PlayerCache.from(player).getCurrentKit().getPowers(player).clear();
                if (this.getPlayers(GameJoinMode.PLAYING).size() == 1) {
                    Player lastPlayer = this.getPlayers(GameJoinMode.PLAYING).get(0).toPlayer();

                    onGameStop();
                    onGameStopMessage(GameStopReason.GAMERS_DISCONNECTED);

                    // Common.runLater(2, () -> {
//                            // Give items as rewards ETC
//                        });
                } //else {
                //player leaves with others still in the game
//                    PlayerCache cache = PlayerCache.from(player);
//                    leaveMsg(cache.toPlayer());
//                }
            });
        }
    }

    private void leaveMsg(Player player) {
        boolean hasWon = player.getUniqueId().equals(winners[0].getUniqueId());

        String winner1 = winners[0] == null ? "" : winners[0].getName() + " &c(0)";
        String winner2 = winners[1] == null ? "" : winners[1].getName() + " &6(0)";
        String winner3 = winners[2] == null ? "" : winners[2].getName() + " &e(0)";

        BoxedMessage.tell(player, "<center>&6&lSuper Smash Mobs\n\n"
                + "<center>&c&l1st Place: &f- " + winner1
                + (winners[1] == null ? "" : "\n" + "<center>&6&l2nd Place &f- " + winner2)
                + (winners[2] == null ? "" : "\n" + "<center>&e&l3rd Place &f- " + winner3) + "\n"
                + " \n"
                + "<center>&6Map - &f" + getName() + " &7created by &fsomeone");

    }

    @Override
    public void onPlayerKill(Player killer, LivingEntity victim, EntityDeathEvent event) {
        super.onPlayerKill(killer, victim, event);

        if (victim instanceof Monster && RandomUtil.chance(100)) {
//            EntityUtil.dropItem(victim.getLocation(), ItemCreator.of(CompMaterial.LAPIS_LAZULI, "&bExperience").make(), item -> {
//                CompMetadata.setMetadata(item, "ItemReward", "true");
//            });
        }
    }

    @Override
    public void onPlayerPickupItem(Player player, PlayerCache cache, Item item) {
        super.onPlayerPickupItem(player, cache, item);

        if (CompMetadata.hasMetadata(item, "ItemReward")) {

            if (RandomUtil.chance(50))
                CompMaterial.GOLDEN_APPLE.give(player, 1);
            else
                CompMaterial.TNT.give(player, 1);

            Messenger.success(player, "You have collected a reward!");
            item.remove();
            this.cancelEvent();
        }
    }

    @Override
    public void onFall(Player player, EntityDamageEvent event) {
        super.onFall(player, event);
    }

    @Override
    public void onBlockPlace(Player player, Block block, BlockPlaceEvent event) {

        if (block.getType() == Material.TNT) {
            TNTPrimed tnt = player.getWorld().spawn(block.getLocation(), TNTPrimed.class);
            tnt.setFuseTicks(3 * 20);

            PlayerUtil.takeFirstOnePiece(player, CompMaterial.TNT);
            this.cancelEvent();
        }

        super.onBlockPlace(player, block, event);
    }

    @Override
    protected boolean canRespawn(Player player, PlayerCache cache) {
        if (cache.getLives() >= 1)
            return true;
        return false;
    }

    @Override
    protected boolean canSpectateOnLeave(Player player) {
        return this.getPlayers(GameJoinMode.PLAYING).size() > 0;
    }
}
