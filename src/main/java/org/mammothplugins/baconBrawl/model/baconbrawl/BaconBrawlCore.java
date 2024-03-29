package org.mammothplugins.baconBrawl.model.baconbrawl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import org.mammothplugins.baconBrawl.BaconBrawl;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.design.PlayerUIDesigns;
import org.mammothplugins.baconBrawl.model.*;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.ElMuchachoPig;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.Kits;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.MamaPiggles;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.Pig;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.nms.NmsDisguise;
import org.mammothplugins.baconBrawl.model.baconbrawl.kits.powers.Power;
import org.mammothplugins.baconBrawl.tool.KitSelectorTool;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.model.BoxedMessage;
import org.mineacademy.fo.remain.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/*
Bacon Brawl
In honor one of Mineplex's best games
Recreated by MammothPlugins (jamesd5)
Started November 1, 2023
 */
public final class BaconBrawlCore extends GameSpawnPoint {

    private Player[] winners = new Player[3];
    public HashMap<UUID, UUID> lastHit = new HashMap<>();

    @Getter
    private boolean canHavePorkalypseMode;
    @Getter
    @Setter
    private boolean porkalypseMode;

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
        this.canHavePorkalypseMode = getBoolean("PorkalyspeMode", true);
        super.onLoad();
    }

    @Override
    protected void onSave() {
        this.set("PorkalyspeMode", this.canHavePorkalypseMode);
        super.onSave();
        this.save();
    }

    public void applyKit(Player player, PlayerCache cache) {
        Kits kit = cache.getCurrentKit();
        kit.applyAttributes(player);
        if (this.porkalypseMode)
            kit.setKnockBack(2.5);
        else
            kit.setKnockBack(1);

        Common.runLater(2, () -> {
            String multiplier = this.porkalypseMode ? "&4X 2.5 &5KnockBack&7!" : "";
            Common.tell(player, Common.colorize("&7You equipped " + kit.getChatColor() + kit.getName() + " Kit " + multiplier));
        });
    }

    public void joinMsg(Player player, boolean isInPorkalyspeMode) {
        if (isInPorkalyspeMode)
            BoxedMessage.tell(player, "<center>&4&lPorkalypseMode\n\n"
                    + "<center>&5Knock other pigs out of the arena!\n"
                    + "<center>&5But be careful, the pigs are Angry!\n"
                    + " \n"
                    + "<center>&4Map - &7" + getName() + " &5created by &7" + getMapCreator());
        else
            BoxedMessage.tell(player, "<center>&6&lBacon Brawl\n\n"
                    + "<center>&7Knock other pigs out of the arena!\n"
                    + "<center>&7Last pig in the arena wins!\n"
                    + " \n"
                    + "<center>&6Map - &f" + getName() + " &7created by &f" + getMapCreator());
    }

    private void leaveMsg(Player player) {
        boolean hasWon = player.getUniqueId().equals(winners[0].getUniqueId());

        String winner1 = winners[0] == null ? "" : winners[0].getName() + " &c(" + PlayerCache.from(winners[0]).getCurrentKills() + ")";
        String winner2 = winners[1] == null ? "" : winners[1].getName() + " &6(" + PlayerCache.from(winners[1]).getCurrentKills() + ")";
        String winner3 = winners[2] == null ? "" : winners[2].getName() + " &e(" + PlayerCache.from(winners[2]).getCurrentKills() + ")";

        BoxedMessage.tell(player, "<center>&6&lBacon Brawl\n\n"
                + "<center>&c&l1st Place: &f- " + winner1
                + (winners[1] == null ? "" : "\n" + "<center>&6&l2nd Place &f- " + winner2)
                + (winners[2] == null ? "" : "\n" + "<center>&e&l3rd Place &f- " + winner3) + "\n"
                + " \n"
                + "<center>&6Map - &f" + getName() + " &7created by &f" + getMapCreator());

    }

    @Override
    protected boolean canSpectateOnLeave(Player player) {
        return this.getPlayers(GameJoinMode.PLAYING).size() > 0;
    }

    public void setcanHavePorkalypseMode(boolean canHavePorkalypseMode) {
        this.canHavePorkalypseMode = canHavePorkalypseMode;
        this.save();
    }

    // ------–------–------–------–------–------–------–------–------–------–------–------–
    // Events
    // ------–------–------–------–------–------–------–------–------–------–------–------–

    @Override
    protected void onGameStartFor(Player player, PlayerCache cache) {
        super.onGameStartFor(player, cache);
        player.setNoDamageTicks(0);

        if (porkalypseMode) {
            CompSound.ENTITY_LIGHTNING_BOLT_THUNDER.play(player);
            player.getWorld().strikeLightning(player.getLocation());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, -1, 1));
        }

    }

    @Override
    protected void onGameJoin(Player player, GameJoinMode mode) {
        super.onGameJoin(player, mode);

        if (mode != GameJoinMode.EDITING)
            KitSelectorTool.getInstance().give(player, 4);
    }

    @Override
    public void leavePlayer(Player player, GameLeaveReason leaveReason) {
        super.leavePlayer(player, leaveReason);
        BaconBrawlScoreboard scoreboard = (BaconBrawlScoreboard) getScoreboard();
        scoreboard.removePlayer(player);

        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.WITHER);
    }

    @Override
    protected void onGameLeave(Player player) {
        super.onGameLeave(player);

        if (this.isPlayed()) {
            player.setNoDamageTicks(10);
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.WITHER);

            //RESETCURRENT
            Common.runLater(2, () -> {
                silentGameResetRequirements(player);
                BaconBrawlScoreboard scoreboard = (BaconBrawlScoreboard) getScoreboard();
                scoreboard.removePlayer(player);
                if (this.getPlayers(GameJoinMode.PLAYING).size() == 1) {
                    theLastPlayer(player);

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

        this.setPorkalypseMode(false);
        this.getRegion().getWorld().setTime(0);

        for (PlayerCache cache : getPlayers(GameJoinMode.PLAYING)) {
            Player player = cache.toPlayer();
            cache.onSave();
            cache.getCurrentKit().wipeAllPowers();
            cache.getCurrentKit().onDeath(player);
            player.setNoDamageTicks(10);
            Common.runLater(2, () -> {
                NmsDisguise.removeDisguise(player);
                player.removePotionEffect(PotionEffectType.REGENERATION);
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.WITHER);
            });
        }
    }

    public void onGameStopMessage(GameStopReason stopReason) {
        if (stopReason == GameStopReason.LAST_PLAYER_LEFT || stopReason == GameStopReason.SILENT_STOP)
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

        BaconBrawlScoreboard scoreboard = (BaconBrawlScoreboard) getScoreboard();
        scoreboard.removePlayer(player);

        //Kills
        PlayerUIDesigns.deathMessage(this, event, lastHit);
        if (lastHit.get(player.getUniqueId()) != null) {
            PlayerCache.from(Bukkit.getPlayer(lastHit.get(player.getUniqueId()))).addCurrentKills();
        }


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
            theLastPlayer(player);
            return;
        }

        if (porkalypseMode) {
            player.getWorld().strikeLightning(player.getLocation());
        }

        cache.setCurrentGameMode(GameJoinMode.SPECTATING);
    }

    void theLastPlayer(Player secondPlacePlayer) {
        this.state = GameState.POSTPLAYED;
        winners[1] = secondPlacePlayer; //2nd Place Winner
        for (PlayerCache playerCache : getPlayers(GameJoinMode.PLAYING))
            if (!(playerCache.getUniqueId().equals(secondPlacePlayer.getUniqueId()))) {
                winners[0] = playerCache.toPlayer(); //1st Place Winner
                playerCache.addGamesWon();
            }

        Common.runLater(2, () -> {

            //FIREWORKS, prob bc cant spawn entities
            AtomicBoolean stopFireworks = new AtomicBoolean(false);
            Common.runLater(20 * 5 - 10, () -> {
                stopFireworks.set(true);
            });

            Player winner = winners[0];
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (stopFireworks.get() == true) {
                        cancel();
                        return;
                    }
                    CompSound.FIREWORK_LAUNCH.play(winner.getLocation());
                    for (int i = 0; i < 50; i++) {
                        CompParticle.FIREWORKS_SPARK.spawn(RandomUtil.nextLocation(winner.getLocation(), 1, true));
                    }
                }
            }.runTaskTimer(BaconBrawl.getInstance(), 0, 20L);


            //GAME MECHANICS
            forEachPlayerInAllModes(player -> {
                PlayerUtil.normalize(player, true);
                teleport(player, getPostGameLocation());
            });
            if (isAutoRotate() == false) {
                onGameStopMessage(GameStopReason.LAST_PLAYER_LEFT);
                stop(GameStopReason.NONAUTO_PRE_STROP);
                Common.runLater(20 * 5, () -> {
                    stop(GameStopReason.LAST_PLAYER_LEFT);
                });

            } else {
                onGameStopMessage(GameStopReason.SILENT_STOP);
                for (PlayerCache cache : getPlayersInAllModes())
                    silentGameResetRequirements(cache.toPlayer());
                stop(GameStopReason.SILENT_STOP);
            }
            Arrays.fill(winners, null); //resets winners Array
        });
    }

    public static void spawnFireworks(Player p) {
        Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        fwm.addEffect(FireworkEffect.builder().withColor(CompColor.PINK.getColor()).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();

        for (int i = 0; i < 1; i++) {
            Firework fw2 = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
            fw2.setFireworkMeta(fwm);
        }
    }

    private void silentGameResetRequirements(Player player) {
        NmsDisguise.removeDisguise(player);
        PlayerCache.from(player).getCurrentKit().getPowers(player).clear();
        PlayerCache.from(player).resetCurrentKills();
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

        if (!(event.getEntity() instanceof Player) || !(event.getEntity() instanceof LivingEntity))
            return;
        PlayerCache dCache = PlayerCache.from(damager);
        Player victim = (Player) event.getEntity();
        PlayerCache victimCache = PlayerCache.from(victim);

        if (dCache.hasGame() && dCache.getCurrentGameMode() == GameJoinMode.PLAYING) {
            event.setDamage(0);
            cancelItemDurability(damager);

            if (porkalypseMode) {
                for (int i = 0; i < 10; i++)
                    CompParticle.REDSTONE.spawn(RandomUtil.nextLocation(victim.getLocation(), 1, true));
            }


            //Death
            lastHit.put(victim.getUniqueId(), damager.getUniqueId());
            victimCache.startCountdownLastKiller(damager);

            //Kits
            if (dCache.getCurrentKit().getPowers(damager).get(0).getName().equals("Cloak")) {
                Pig.CloakPower cloakPower = (Pig.CloakPower) dCache.getCurrentKit().getPowers(damager).get(0);
                if (cloakPower.canPostActiavteMelee()) {
                    if (event.getEntity() instanceof Player)
                        cloakPower.postActivatedMelee((Player) event.getEntity(), event);
                } else
                    event.getEntity().setVelocity(damager.getLocation().getDirection().setY(0).normalize().multiply(dCache.getCurrentKit().getKnockBack()));
            } else
                event.getEntity().setVelocity(damager.getLocation().getDirection().setY(0).normalize().multiply(dCache.getCurrentKit().getKnockBack()));
            if (victimCache.getCurrentKit().getName().equals("Pig")) {
                Pig pig = (Pig) victimCache.getCurrentKit();
                Pig.CloakPower cloakPower = (Pig.CloakPower) pig.getPowers(victim).get(0);
                cloakPower.revealPlayer(victim);
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

            for (PlayerCache ch : cache.getCurrentGame().getPlayers(GameJoinMode.PLAYING)) {
                if (ch.toPlayer().getUniqueId().equals(victim.getUniqueId()))
                    bodySlamPower.postActivatedMelee(victim);
            }
        }
    }

    @Override
    public void onCooldown(PlayerInteractEvent event) {
        super.onCooldown(event);
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);
        Action action = event.getAction();
        if (cache.hasGame() && cache.getCurrentGameMode() == GameJoinMode.PLAYING) {
            if (cache.hasGame() && cache.getCurrentGame().getState() == GameState.PREPLAYED)
                return;
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

    @Override
    public void onBlockMine(BlockBreakEvent event) {
        super.onBlockMine(event);
        cancelItemDurability(event.getPlayer());
    }

    private void cancelItemDurability(Player player) {
        if (player.getItemInHand() != null && player.getItemInHand().getItemMeta() instanceof Damageable) {
            ItemStack itemStack = player.getItemInHand();
            itemStack.setDurability((short) 0);

            player.getInventory().remove(itemStack);
            player.getInventory().setItemInHand(itemStack);
        }
    }
}
