package org.mammothplugins.baconBrawl.model.baconbrawl;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.model.*;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.ElMuchachoPig;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.Kits;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.MamaPiggles;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.nms.NmsDisguise;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers.Power;
import org.mammothplugins.baconBrawl.tool.KitSelectorTool;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.model.BoxedMessage;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.Remain;

import java.util.Arrays;

public final class BaconBrawlCore extends GameSpawnPoint {

    private Player[] winners = new Player[3];

    protected BaconBrawlCore(String name) {
        super(name);
    }

    protected BaconBrawlCore(String name, @Nullable GameType type) {
        super(name, type);
    }

    @Override
    protected GameHeartbeat compileHeartbeat() {
        return new BaconBrawlHeartBeat(this);
    }

    @Override
    protected GameScoreboard compileScoreboard() {
        return new BaconBrawlScoreboard(this);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
    }

    @Override
    protected void onSave() {
        super.onSave();
    }

    public void applyKit(Player player, PlayerCache cache) {
        Kits kit = cache.getCurrentKit();
        kit.applyAttributes(player);

        Common.tell(player, Common.colorize("&7You equipped " + kit.getChatColor() + kit.getName() + " Kit"));
    }

    private void leaveMsg(Player player) {
        boolean hasWon = player.getUniqueId().equals(winners[0].getUniqueId());

        String winner1 = winners[0] == null ? "" : winners[0].getName() + " &c(0)";
        String winner2 = winners[1] == null ? "" : winners[1].getName() + " &6(0)";
        String winner3 = winners[2] == null ? "" : winners[2].getName() + " &e(0)";

        BoxedMessage.tell(player, "<center>&6&lBacon Brawl\n\n"
                + "<center>&c&l1st Place: &f- " + winner1
                + (winners[1] == null ? "" : "\n" + "<center>&6&l2nd Place &f- " + winner2)
                + (winners[2] == null ? "" : "\n" + "<center>&e&l3rd Place &f- " + winner3) + "\n"
                + " \n"
                + "<center>&6Map - &f" + getName() + " &7created by &fsomeone");

    }

    @Override
    protected boolean canSpectateOnLeave(Player player) {
        return this.getPlayers(GameJoinMode.PLAYING).size() > 0;
    }


    // ------–------–------–------–------–------–------–------–------–------–------–------–
    // Events
    // ------–------–------–------–------–------–------–------–------–------–------–------–
    @Override
    protected void onGameStartFor(Player player, PlayerCache cache) {
        super.onGameStartFor(player, cache);
    }

    @Override
    protected void onGameJoin(Player player, GameJoinMode mode) {
        super.onGameJoin(player, mode);

        KitSelectorTool.getInstance().give(player, 4);
    }

    @Override
    protected void onGameLeave(Player player) {
        super.onGameLeave(player);

        if (this.isPlayed()) {
            player.removePotionEffect(PotionEffectType.REGENERATION);
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

    @Override
    protected void onGameStop() {
        super.onGameStop();

        for (PlayerCache cache : getPlayers(GameJoinMode.PLAYING)) {
            cache.toPlayer().removePotionEffect(PotionEffectType.REGENERATION);
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
    public void onPlayerKill(Player killer, LivingEntity victim, EntityDeathEvent event) {
        super.onPlayerKill(killer, victim, event);

        if (victim instanceof Monster && RandomUtil.chance(100)) {
//            EntityUtil.dropItem(victim.getLocation(), ItemCreator.of(CompMaterial.LAPIS_LAZULI, "&bExperience").make(), item -> {
//                CompMetadata.setMetadata(item, "ItemReward", "true");
//            });
        }
    }

    @Override
    public void onPlayerDeath(PlayerCache cache, PlayerDeathEvent event) {
        super.onPlayerDeath(cache, event);
        Player player = event.getEntity();

        cache.getCurrentKit().onDeath(player);
        for (Power power : PlayerCache.from(player).getCurrentKit().getPowers(player))
            power.resetPower();
        //PlayerUIDesigns.deathMessage(cache.getCurrentGame(), event);

        // No Respawn Screen
        Remain.respawn(player);
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
    }

    @Override
    public void onFall(EntityDamageEvent event) {
        super.onFall(event);

        event.setCancelled(true);
//        if (gameAtLocation == null || !(event.getEntity() instanceof Player))
//            return;
//
//        if (!gameAtLocation.isEdited() && !gameAtLocation.isPlayed()) {
//            event.setCancelled(true);
//            return;
//        }
//        Player player = (Player) event.getEntity();
//
//        final PlayerCache cache = gameAtLocation.findPlayer(player);
//
//        if (cache == null || cache.getCurrentGameMode() == GameJoinMode.SPECTATING)
//            event.setCancelled(true);
//
//        if (cache != null && cache.getCurrentGameMode() == GameJoinMode.PLAYING) {
//            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
//                gameAtLocation.onFall(player, event);
//                event.setCancelled(true);
//            }
//        }
    }

    @Override
    public void onPlayerMeleeAttack(EntityDamageByEntityEvent event, Player damager) {
        super.onPlayerMeleeAttack(event, damager);

        if (event.getEntity() instanceof LivingEntity) {
            PlayerCache dCache = PlayerCache.from(damager);
            if (dCache.hasGame() && dCache.getCurrentGameMode() == GameJoinMode.PLAYING) {
                event.setDamage(0);
                event.getEntity().setVelocity(damager.getLocation().getDirection().setY(0).normalize().multiply(dCache.getCurrentKit().getKnockBack()));
            }
        }
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        super.onProjectileHit(event);

        Player shooter = ((Player) event.getEntity().getShooter()).getPlayer();
        Kits shooterKit = PlayerCache.from(shooter).getCurrentKit();
        if (event.getEntity() instanceof Snowball) {
            if (CompMetadata.hasTempMetadata(event.getEntity(), "PorkBomb")) {
                MamaPiggles.BaconBlast baconBlast = (MamaPiggles.BaconBlast) shooterKit.getPowers(shooter).get(0);
                if (event.getHitEntity() instanceof LivingEntity)
                    baconBlast.postActivatedProjectile((LivingEntity) event.getHitEntity(), event.getEntity());
            }
        }
    }

    @Override
    public void onPlayerCollideWithOtherPlayerEvent(PlayerMoveEvent event, Player victim) {
        super.onPlayerCollideWithOtherPlayerEvent(event, victim);

        PlayerCache cache = PlayerCache.from(event.getPlayer());

        if (cache.getCurrentKit().getPowers(event.getPlayer()).get(0).getName().equals("Body Slam")) {
            ElMuchachoPig.BodySlamPower bodySlamPower = (ElMuchachoPig.BodySlamPower) cache.getCurrentKit().getPowers(event.getPlayer()).get(0);

            bodySlamPower.postActivatedMelee(victim);
        }
    }

    @Override
    public void onCooldown(PlayerInteractEvent event) {
        super.onCooldown(event);
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);
        Action action = event.getAction();
        if (cache.hasGame() && cache.getCurrentGameMode() == GameJoinMode.PLAYING) {
            if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
                for (Power power : cache.getCurrentKit().getPowers(player)) {
                    if (player.getItemInHand().getType() == power.getItemStack().getType()) {
                        if (power.isUsesBlocking())
                            if (cache.isCurrentlyBlocking() == false)
                                power.startBlocking();
                        if (!power.isCoolingDown()) {
                            power.startPowerCooldowns();
                            cache.getCurrentKit().usePower(player, power);
                        } else {
                            if (power.canStartCooldown() && cache.isCurrentlyBlocking() == false)
                                Common.tell(player, "&7You cannot use &a" + power.getName() + "&7 for &a" + power.getConvertedTimeLeftCooldown() + " Seconds&7.");
                        }
                    }
                }
            }
        }
    }
}
