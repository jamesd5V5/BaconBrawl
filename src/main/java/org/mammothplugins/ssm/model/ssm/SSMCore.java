package org.mammothplugins.ssm.model.ssm;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.SSM;
import org.mammothplugins.ssm.design.PlayerUIDesigns;
import org.mammothplugins.ssm.model.*;
import org.mammothplugins.ssm.model.ssm.kits.Creeper;
import org.mammothplugins.ssm.model.ssm.kits.Kits;
import org.mammothplugins.ssm.model.ssm.kits.MamaPiggles;
import org.mammothplugins.ssm.model.ssm.kits.nms.NmsDisguise;
import org.mammothplugins.ssm.model.ssm.kits.powers.Power;
import org.mammothplugins.ssm.tool.KitSelectorTool;
import org.mammothplugins.ssm.tool.SpectatePlayersTool;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.BoxedMessage;
import org.mineacademy.fo.remain.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
Bacon Brawl
In honor one of Mineplex's best games
Recreated by MammothPlugins (jamesd5)
Started November 1, 2023
 */
public final class SSMCore extends GameSpawnPoint {

    private Player[] winners = new Player[3];
    public HashMap<UUID, UUID> lastHit = new HashMap<>();

    protected SSMCore(String name) {
        super(name);
    }

    protected SSMCore(String name, @Nullable GameType type) {
        super(name, type);
    }

    @Override
    protected GameHeartbeat compileHeartbeat() {
        return new SSMHeartBeat(this);
    }

    @Override
    protected GameScoreboard compileScoreboard() {
        return new SSMScoreBoard(this);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
    }

    @Override
    protected void onSave() {
        super.onSave();
        this.save();
    }

    public void applyKit(Player player, PlayerCache cache) {
        Kits kit = cache.getCurrentKit();
        kit.applyAttributes(player);

        Common.runLater(2, () -> {
            Common.tell(player, Common.colorize("&7You equipped " + kit.getChatColor() + kit.getName() + " Kit "));
        });
    }

    public void joinMsg(Player player) {
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

        BoxedMessage.tell(player, "<center>&6&lSSM\n\n"
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

    // ------–------–------–------–------–------–------–------–------–------–------–------–
    // Events
    // ------–------–------–------–------–------–------–------–------–------–------–------–

    @Override
    protected void onGameStartFor(Player player, PlayerCache cache) {
        super.onGameStartFor(player, cache);
        cache.setLives(4);
        player.setNoDamageTicks(0);
    }

    @Override
    protected void onGameJoin(Player player, GameJoinMode mode) {
        super.onGameJoin(player, mode);

        if (mode != GameJoinMode.EDITING) {
            KitSelectorTool.getInstance().give(player, 4);
            PlayerCache cache = PlayerCache.from(player);
            cache.setFreshKit(cache.getCurrentKit().getName());
        }
    }

    @Override
    public void leavePlayer(Player player, GameLeaveReason leaveReason) {
        super.leavePlayer(player, leaveReason);
        SSMScoreBoard scoreboard = (SSMScoreBoard) getScoreboard();
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
                SSMScoreBoard scoreboard = (SSMScoreBoard) getScoreboard();
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
        cache.setLives(cache.getLives() - 1);

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

        int currentLives = cache.getLives();
        SSMScoreBoard scoreboard = (SSMScoreBoard) getScoreboard();

        if (currentLives <= 0) {
            scoreboard.removePlayer(player);

            int remainingPlayers = getPlayers(GameJoinMode.PLAYING).size();

            if (remainingPlayers == 3)
                winners[2] = player; //3rd Place Winner
            if (remainingPlayers <= 2) {
                theLastPlayer(player);
                return;
            }

            cache.setCurrentGameMode(GameJoinMode.SPECTATING);
        } else {
            startDeathSpectate(player);
        }

        //===========================================================
        //                      Kits
        //===========================================================

        Player victim = player;
        Player attacker = victim.getKiller();

        if (attacker == null)
            return;

        PlayerCache victimCache = PlayerCache.from(victim);
        PlayerCache attackerCache = PlayerCache.from(attacker);

        if (attackerCache == null || attackerCache.getCurrentGameMode() == GameJoinMode.SPECTATING ||
                victimCache == null || victimCache.getCurrentGameMode() == GameJoinMode.SPECTATING)
            return;


        if (attackerCache != null && attackerCache.getCurrentGameMode() == GameJoinMode.PLAYING &&
                victimCache != null || victimCache.getCurrentGameMode() == GameJoinMode.PLAYING) {
            Kits victimsKit = victimCache.getCurrentKit();
            Kits attackersKit = attackerCache.getCurrentKit();


            //MagmaCube Kit
            //if (attackersKit instanceof org.mammothplugins.ssm.model.ssm.kits.MagmaCube)
            // ((org.mammothplugins.ssm.model.ssm.kits.MagmaCube) attackersKit).onKill();
        }
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
        }.runTaskTimer(SSM.getInstance(), 0L, 20L);
        Common.runLater(4 * 20 + 1, () -> {
            cache.getCurrentKit().applyAttributes(player);
            Common.tell(player, "&7You respawned as a " + cache.getCurrentKit().getName() + ".");

            cache.removePlayerTag("InSpectateDeath");
            untransfromToDeathSpectate(player);
        });
    }

    protected void transformToDeathSpectate(Player player) {
        PlayerCache cache = PlayerCache.from(player);

        forEachPlayerInAllModes(other -> other.hidePlayer(player));

        cache.setPlayerTag("AllowGamemodeChange", true);
        player.setGameMode(GameMode.ADVENTURE);
        cache.removePlayerTag("AllowGamemodeChange");

        player.setAllowFlight(true);
        player.setFlying(true);

        Common.runLater(2, () -> {
            this.teleport(player, getDeathSpawnLocation());
        });
    }

    protected void untransfromToDeathSpectate(Player player) {
        PlayerCache cache = PlayerCache.from(player);

        forEachPlayerInAllModes(other -> other.showPlayer(player));

        cache.setPlayerTag("AllowGamemodeChange", true);
        player.setGameMode(GameMode.ADVENTURE);
        cache.removePlayerTag("AllowGamemodeChange");

        player.setAllowFlight(false);
        player.setFlying(false);

        if (this instanceof GameSpawnPoint) {
            GameSpawnPoint spawnpointGame = (GameSpawnPoint) this;
            Location spawnpoint = spawnpointGame.getPlayerSpawnpointPicker().pickRandom();

            teleport(player, spawnpoint);
        }
    }

    protected void transformToSpectate(Player player) {
        PlayerCache cache = PlayerCache.from(player);

        cache.setCurrentGameMode(GameJoinMode.SPECTATING);

        // Normalize once again, clearing all items
        PlayerUtil.normalize(player, true);

        // Set invisibility
        forEachPlayerInAllModes(other -> other.hidePlayer(player));

        // Set adventure and flying
        cache.setPlayerTag("AllowGamemodeChange", true);
        player.setGameMode(GameMode.ADVENTURE);
        cache.removePlayerTag("AllowGamemodeChange");

        player.setAllowFlight(true);
        player.setFlying(true);

        // Teleport to the first living player
        final List<Player> playingPlayers = this.getBukkitPlayers(GameJoinMode.PLAYING);
        Valid.checkBoolean(!playingPlayers.isEmpty(), "Cannot spectate arena where there are no playing players! Found: " + playingPlayers);
        final Player randomPlayer = RandomUtil.nextItem(playingPlayers);

        this.teleport(player, randomPlayer.getPlayer().getLocation().add(0.5, 1, 0.5));
        player.setCompassTarget(randomPlayer.getLocation());

        // Give a special compass that opens a menu to select players to teleport to
        SpectatePlayersTool.getInstance().give(player, 4);

        this.getScoreboard().onSpectateStart();
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
            }.runTaskTimer(SSM.getInstance(), 0, 20L);


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

            org.bukkit.util.Vector playerDirection = player.getLocation().getDirection();

            if (playerDirection.getY() <= -0.9) {
                // Looking down, jump opposite direction
                org.bukkit.util.Vector oppositeDirection = playerDirection.multiply(-1);
                org.bukkit.util.Vector jumpVector = oppositeDirection.multiply(0.9).add(new org.bukkit.util.Vector(0, 0.10, 0));
                player.setVelocity(jumpVector);
            } else {
                // Looking forward or up, jump in the same direction
                org.bukkit.util.Vector jumpVector = playerDirection.multiply(0.9).add(new Vector(0, 0.5, 0));
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

        if (!(event.getEntity() instanceof LivingEntity))
            return;
        PlayerCache dCache = PlayerCache.from(damager);

        if (dCache.hasGame() && dCache.getCurrentGameMode() == GameJoinMode.PLAYING) {
            if (!damager.getItemInHand().getType().toString().equals("AIR")) {
                double damage = dCache.getCurrentKit().getDamage();
                event.setDamage(damage);
            } else {
                event.setDamage(1);
            }
            cancelItemDurability(damager);

            //Death
            if (!(event.getEntity() instanceof Player))
                return;
            Player victim = (Player) event.getEntity();
            PlayerCache victimCache = PlayerCache.from(victim);
            lastHit.put(victim.getUniqueId(), damager.getUniqueId());
            victimCache.startCountdownLastKiller(damager);

            //Kits
//            if (dCache.getCurrentKit().getPowers(damager).get(0).getName().equals("Cloak")) {
//                Pig.CloakPower cloakPower = (Pig.CloakPower) dCache.getCurrentKit().getPowers(damager).get(0);
//                if (cloakPower.canPostActiavteMelee()) {
//                    if (event.getEntity() instanceof Player)
//                        cloakPower.postActivatedMelee((Player) event.getEntity(), event);
//                } else
//                    event.getEntity().setVelocity(damager.getLocation().getDirection().setY(0).normalize().multiply(dCache.getCurrentKit().getKnockBack()));
//            } else
//                event.getEntity().setVelocity(damager.getLocation().getDirection().setY(0).normalize().multiply(dCache.getCurrentKit().getKnockBack()));
//            if (victimCache.getCurrentKit().getName().equals("Pig")) {
//                Pig pig = (Pig) victimCache.getCurrentKit();
//                Pig.CloakPower cloakPower = (Pig.CloakPower) pig.getPowers(victim).get(0);
//                cloakPower.revealPlayer(victim);
//            }
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
            if (CompMetadata.hasTempMetadata(event.getEntity(), "SulpherBomb")) {
                Creeper.SulpherBombPower sulpherBombPower = (Creeper.SulpherBombPower) shooterKit.getPowers(shooter).get(0);
                if (event.getHitEntity() instanceof LivingEntity)
                    sulpherBombPower.postActivatedProjectile((LivingEntity) event.getHitEntity(), event.getEntity());
            }
            if (CompMetadata.hasTempMetadata(event.getEntity(), "InfernoPowder")) {
                org.mammothplugins.ssm.model.ssm.kits.Blaze.InfernoPower infernoPower = (org.mammothplugins.ssm.model.ssm.kits.Blaze.InfernoPower) shooterKit.getPowers(shooter).get(0);
                if (event.getHitEntity() instanceof LivingEntity)
                    infernoPower.postActivatedProjectile((LivingEntity) event.getHitEntity(), event.getEntity());
            }
            if (CompMetadata.hasTempMetadata(event.getEntity(), "ThrownBlock")) {
                org.mammothplugins.ssm.model.ssm.kits.Enderman.BlockToss blockToss = (org.mammothplugins.ssm.model.ssm.kits.Enderman.BlockToss) shooterKit.getPowers(shooter).get(0);
                if (event.getHitEntity() instanceof LivingEntity)
                    blockToss.postActivatedProjectile((LivingEntity) event.getHitEntity(), event.getEntity());
            }
        }
        if (event.getEntity() instanceof Fireball) {
            if (CompMetadata.hasTempMetadata(event.getEntity(), "MagmaBlast")) {
                org.mammothplugins.ssm.model.ssm.kits.MagmaCube.MagmaBlastPower magmaBlastPower = (org.mammothplugins.ssm.model.ssm.kits.MagmaCube.MagmaBlastPower) shooterKit.getPowers(shooter).get(0);
                if (event.getHitEntity() instanceof LivingEntity)
                    magmaBlastPower.postActivatedProjectile((LivingEntity) event.getHitEntity(), event.getEntity());
                if (event.getHitEntity().getUniqueId().equals(shooter.getUniqueId()))
                    event.setCancelled(true); //gotta get explosion event so that the player does not dmg itself, also only creates explosion effect when play get hits, maybe with explosion update this will worj
            }
        }
        if (event.getEntity() instanceof WitherSkull) {
            if (CompMetadata.hasTempMetadata(event.getEntity(), "WitherSkull")) {
                org.mammothplugins.ssm.model.ssm.kits.WitherSkeleton.GuidedWitherSkullPower witherSkullPower = (org.mammothplugins.ssm.model.ssm.kits.WitherSkeleton.GuidedWitherSkullPower) shooterKit.getPowers(shooter).get(0);
                if (event.getHitEntity() instanceof LivingEntity)
                    witherSkullPower.postActivatedProjectile((LivingEntity) event.getHitEntity(), event.getEntity());
            }
        }
        if (event.getHitEntity() instanceof Player) { //When the Creeper is hit
            Player player = (Player) event.getHitEntity();
            final PlayerCache cache = Game.findByLocation(event.getHitEntity().getLocation()).findPlayer(player);

            if (cache == null || cache.getCurrentGameMode() == GameJoinMode.SPECTATING)
                event.setCancelled(true);

            if (cache != null && cache.getCurrentGameMode() == GameJoinMode.PLAYING && cache.getCurrentGame() instanceof SSMCore) {
                Kits kits = cache.getCurrentKit();
                if (kits instanceof org.mammothplugins.ssm.model.ssm.kits.Creeper)
                    ((org.mammothplugins.ssm.model.ssm.kits.Creeper) kits).onProjectileHit(player);
            }
        }

        if (!(event.getEntity() instanceof Player))
            return;
        Player victim = (Player) event.getEntity();
        PlayerCache victimCache = PlayerCache.from(victim);
        lastHit.put(victim.getUniqueId(), shooter.getUniqueId());
        // victim.setLastDamageCause();
        victimCache.startCountdownLastKiller(shooter);
    }

    @Override
    public void onPlayerCollideWithOtherPlayerEvent(PlayerMoveEvent event, Player victim) {
        super.onPlayerCollideWithOtherPlayerEvent(event, victim);

        PlayerCache cache = PlayerCache.from(event.getPlayer());

//        if (cache.getCurrentKit().getPowers(event.getPlayer()).get(0).getName().equals("Body Slam")) {
//            ElMuchachoPig.BodySlamPower bodySlamPower = (ElMuchachoPig.BodySlamPower) cache.getCurrentKit().getPowers(event.getPlayer()).get(0);
//
//            for (PlayerCache ch : cache.getCurrentGame().getPlayers(GameJoinMode.PLAYING)) {
//                if (ch.toPlayer().getUniqueId().equals(victim.getUniqueId()))
//                    bodySlamPower.postActivatedMelee(victim);
//            }
//        }
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
