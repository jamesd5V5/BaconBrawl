package org.mammothplugins.ssm.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.vehicle.*;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.Game;
import org.mammothplugins.ssm.model.GameJoinMode;
import org.mammothplugins.ssm.model.GameLeaveReason;
import org.mammothplugins.ssm.model.GameState;
import org.mammothplugins.ssm.model.ssm.SSMCore;
import org.mammothplugins.ssm.model.ssm.kits.Kits;
import org.mammothplugins.ssm.settings.Settings;
import org.mineacademy.fo.*;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.event.RocketExplosionEvent;
import org.mineacademy.fo.exception.EventHandledException;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@AutoRegister
public final class AntiGriefListeners implements Listener {

    private static final Set<String> ENTITY_TYPE_MANIPULATION_BLACKLIST = Common.newSet("ITEM_FRAME", "PAINTING", "ARMOR_STAND", "LEASH_HITCH");

    public AntiGriefListeners() {
        registerEvent("org.bukkit.event.player.PlayerItemConsumeEvent", ConsumeItemListener::new);
        registerEvent("org.bukkit.event.player.PlayerBucketEntityEvent", PlayerBucketEntityListener::new,
                "org.bukkit.event.player.PlayerBucketFishEvent", PlayerBucketFishListener::new);

        registerEvent("org.bukkit.event.player.PlayerTakeLecternBookEvent", PlayerTakeLecternBookListener::new);

        registerEvent("org.bukkit.event.entity.EntityEnterBlockEvent", EntityEnterBlockListener::new);
        registerEvent("org.bukkit.event.entity.EntityPickupItemEvent", EntityPickupItemListener::new,
                "org.bukkit.event.player.PlayerPickupItemEvent", PlayerPickupItemListener::new);

        registerEvent("org.bukkit.event.entity.EntityBreedEvent", EntityBreedListener::new);
        registerEvent("org.bukkit.event.entity.SpawnerSpawnEvent", SpawnerSpawnListener::new);

        registerEvent("org.bukkit.event.block.BlockReceiveGameEvent", BlockReceiveGameListener::new);
        registerEvent("org.bukkit.event.block.BlockExplodeEvent", ExplodeAndEntitySpawnListener::new);
        registerEvent("org.bukkit.event.block.CauldronLevelChangeEvent", CauldronLevelChangeListener::new);

        registerEvent("org.bukkit.event.raid.RaidTriggerEvent", RaidTriggerListener::new);
    }

    private void registerEvent(String classPath, Supplier<Listener> listener) {
        this.registerEvent(classPath, listener, null, null);
    }

    private void registerEvent(String classPath, Supplier<Listener> listener, @Nullable String fallbackClass, @Nullable Supplier<Listener> fallbackListener) {
        try {
            Class.forName(classPath);

            Common.registerEvents(listener.get());

        } catch (final ClassNotFoundException ex) {
            if (fallbackListener != null)
                try {
                    Class.forName(fallbackClass);

                    Common.registerEvents(fallbackListener.get());
                } catch (final Throwable t) {
                    // Completely unavailable
                }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Game game = Game.findByLocation(player.getLocation());

        PlayerCache.from(player); // Load player's cache

        if (Settings.AutoMode.ENABLED) {
            Game firstGame = Game.getGames().get(0);
            boolean successfullyJoined;

            if (firstGame.isPlayed())
                successfullyJoined = firstGame.joinPlayer(player, GameJoinMode.SPECTATING);
            else
                successfullyJoined = firstGame.joinPlayer(player, GameJoinMode.PLAYING);

            if (!successfullyJoined)
                Common.runLater(2, () -> BungeeUtil.connect(player, Settings.AutoMode.RETURN_BACK_SERVER));

            return;
        }

        if (game != null) {
            Valid.checkBoolean(!game.isJoined(player), "Found disconnected dude " + player.getName() + " in game " + game.getName() + " that just joined the server");

            if (!player.isOp()) {
                game.teleportToReturnLocation(player);

                Messenger.warn(player, "You have been teleported away from a stopped game's region.");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Game game = Game.findByLocation(player.getLocation());
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame())
            cache.getCurrentGame().leavePlayer(player, GameLeaveReason.QUIT_SERVER);

        cache.save();

        cache.removeFromMemory();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame() && cache.getCurrentGame().getState() == GameState.PREPLAYED)
            event.setCancelled(true);
        if (cache.hasGame())
            try {
                cache.getCurrentGame().onPlayerChat(cache, event);

            } catch (EventHandledException ex) {
                event.setCancelled(ex.isCancelled());
            }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame())
            try {
                cache.getCurrentGame().onPlayerDeath(cache, event);

            } catch (EventHandledException ex) {
                // Handled upstream
            }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame())
            try {
                cache.setPlayerTag("Respawning", event);
                cache.getCurrentGame().onPlayerRespawn(cache, event);

                if (cache.hasPlayerTag("Respawning"))
                    cache.removePlayerTag("Respawning");

            } catch (EventHandledException ex) {
                // Handled upstream
            }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMeleeAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player player = (Player) event.getDamager();
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame() && cache.getCurrentGame().getState() == GameState.PREPLAYED)
            event.setCancelled(true);

        if (cache.hasGame() && cache.getCurrentGame().isPlayed() && !cache.getCurrentGame().isStopped())
            try {
                cache.getCurrentGame().onPlayerMeleeAttack(event, player);

            } catch (EventHandledException ex) {
            }
    }

    @EventHandler
    public void onCooldown(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame() && cache.getCurrentGame().getState() == GameState.PREPLAYED)
            event.setCancelled(true);

        if (cache.hasGame())
            try {
                cache.getCurrentGame().onCooldown(event);

            } catch (EventHandledException ex) {
            }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);
        GameJoinMode mode = cache.getCurrentGameMode();

        if (cache.hasGame() && cache.getCurrentGame().getState() == GameState.PREPLAYED)
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
                Location loc = event.getFrom();
                event.getPlayer().teleport(loc.setDirection(event.getTo().getDirection()));
            }
        if (cache.hasGame() && cache.getCurrentGameMode() == GameJoinMode.PLAYING) {
            cache.getCurrentGame().onPlayerMoveEvent(event);
            cache.getCurrentGame().onPlayerLand(player, event);
        }
    }

    @EventHandler
    public void onPlayerInteractWith(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);
        if (cache.hasGame() && cache.getCurrentGame().getState() == GameState.PREPLAYED)
            event.setCancelled(true);
        if (cache.hasGame() && cache.getCurrentGameMode() == GameJoinMode.PLAYING) {
            if (event.hasItem())
                cache.getCurrentGame().onPlayerInteractItem(event.getItem(), event);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame())
            try {
                cache.getCurrentGame().onPlayerCommand(cache, event);

            } catch (EventHandledException ex) {
                event.setCancelled(ex.isCancelled());
            }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Game game = Game.findByLocation(event.hasBlock() ? event.getClickedBlock().getLocation() : player.getLocation());

        if (game != null) {
            PlayerCache gamePlayer = game.findPlayer(player);

            if (gamePlayer == null) {

                if (!action.toString().contains("AIR") && action != Action.PHYSICAL && !Remain.isInteractEventPrimaryHand(event))
                    Messenger.warn(player, "Use '/game edit' to make changes to this game.");

                event.setCancelled(true);
                player.updateInventory();
            } else {
                try {
                    if (PlayerCache.from(player).hasGame() && PlayerCache.from(player).getCurrentGame().getState() == GameState.PREPLAYED)
                        event.setCancelled(true);
                    gamePlayer.getCurrentGame().onPlayerInteract(gamePlayer, event);

                } catch (EventHandledException ex) {
                    event.setCancelled(ex.isCancelled());
                }
            }
        }
    }

    @EventHandler
    public void onInteractAtEntity(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final Entity entity = event.getRightClicked();
        final Game arena = Game.findByLocation(entity.getLocation());

        if (arena != null) {
            final PlayerCache cache = arena.findPlayer(player);

            if (cache == null || cache.getCurrentGameMode() == GameJoinMode.SPECTATING) {
                event.setCancelled(true);

                return;
            }

            try {
                if (cache.hasGame() && cache.getCurrentGame().getState() == GameState.PREPLAYED)
                    event.setCancelled(true);
                arena.onEntityClick(player, entity, event);

            } catch (final EventHandledException ex) {
                event.setCancelled(ex.isCancelled());
            }
        }
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        this.executeIfPlayingGame(event, (player, cache) -> {
            if (!cache.isJoining() && !cache.isLeaving() && !cache.hasPlayerTag("AllowGamemodeChange"))
                event.setCancelled(true);
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerCache cache = PlayerCache.from(player);
        GameJoinMode mode = cache.getCurrentGameMode();

        if (cache.hasGame() && mode != GameJoinMode.EDITING)
            if (cache.hasGame() && mode != GameJoinMode.EDITING && Menu.getMenu(player) == null)
                event.setCancelled(event.isCancelled());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerCache cache = PlayerCache.from(player);
        GameJoinMode mode = cache.getCurrentGameMode();

        if (cache.hasGame() && mode != GameJoinMode.EDITING && Menu.getMenu(player) == null)
            event.setCancelled(event.isCancelled());

    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        this.executeIfPlayingGame(event, (player, cache) -> event.setAmount(0));
    }

    @EventHandler
    public void onBedEnter(final PlayerBedEnterEvent event) {
        final Game arena = Game.findByLocation(event.getBed().getLocation());

        if (arena != null) {
            final PlayerCache cache = arena.findPlayer(event.getPlayer());

            if (cache == null || cache.getCurrentGameMode() == GameJoinMode.SPECTATING || cache.getCurrentGame().getState() == GameState.PREPLAYED)
                event.setCancelled(true);

            else if (cache.hasGame())
                try {
                    cache.getCurrentGame().onBedEnter(cache, event);

                } catch (EventHandledException ex) {
                    event.setCancelled(ex.isCancelled());
                }
        }
    }

    @EventHandler
    public void onVehicleCreate(final VehicleCreateEvent event) {
        if (event instanceof Cancellable) // Compatibility with older MC versions
            this.cancelIfInStoppedOrLobby(event, event.getVehicle());
    }

    @EventHandler
    public void onVehicleEnter(final VehicleEnterEvent event) {
        preventVehicleGrief(event, event.getEntered());
    }

    @EventHandler
    public void onVehicleDamage(final VehicleDamageEvent event) {
        preventVehicleGrief(event, event.getAttacker());
    }

    @EventHandler
    public void onVehicleDestroy(final VehicleDestroyEvent event) {
        preventVehicleGrief(event, event.getAttacker());
    }

    @EventHandler
    public void onVehicleCollision(final VehicleEntityCollisionEvent event) {
        preventVehicleGrief(event, event.getEntity());
    }

    private <T extends VehicleEvent & Cancellable> void preventVehicleGrief(final T event, final Entity involvedEntity) {
        final Game game = Game.findByLocation(event.getVehicle().getLocation());

        if (game != null) {
            if (!game.isPlayed() && !game.isEdited()) {
                event.setCancelled(true);

                return;
            }

            if (involvedEntity instanceof Player) {
                final PlayerCache cache = PlayerCache.from((Player) involvedEntity);

                if (!cache.hasGame() || cache.getCurrentGameMode() == GameJoinMode.SPECTATING)
                    event.setCancelled(true);

            } else {
                final Game involvedArena = Game.findByLocation(involvedEntity.getLocation());

                if (involvedArena == null || !involvedArena.equals(game))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustByEntityEvent event) {
        this.cancelIfInStoppedOrLobby(event, event.getCombuster());
    }

    @EventHandler
    public void onEntityAttacked(final EntityDamageByEntityEvent event) {
        final Entity victim = event.getEntity();
        final Entity attacker = event.getDamager();

        final Game victimGame = Game.findByLocation(victim.getLocation());
        final Game attackerGame = Game.findByLocation(attacker.getLocation());

        // Prevent other players from attacking arena players
        if (attackerGame != null && victimGame == null && attackerGame.isPlayed() && attacker instanceof Projectile) {
            final Projectile arrow = (Projectile) attacker;

            if (victim instanceof Player && arrow.getShooter() instanceof Player) {
                event.setCancelled(true);

                return;
            }
        }

        if (victimGame == null)
            return;

        if (!victimGame.isPlayed() && !victimGame.isEdited()) {
            event.setCancelled(true);

            return;
        }

        if (attackerGame == null || !attackerGame.equals(victimGame)) {
            event.setCancelled(true);

            return;
        }

        if (attacker instanceof Player) {
            final PlayerCache attackerCache = victimGame.findPlayer((Player) attacker);

            if (attackerCache == null || !attackerCache.hasGame() || !attackerCache.getCurrentGame().equals(victimGame)) {
                event.setCancelled(true);

                return;
            }

            if (attackerCache.getCurrentGameMode() == GameJoinMode.SPECTATING)
                event.setCancelled(true);

            else if (victim instanceof Player) {
                if (!victimGame.hasPvP()) {
                    event.setCancelled(true);

                    return;
                }

                try {
                    victimGame.onPvP((Player) attacker, (Player) victim, event);
                } catch (final EventHandledException ex) {
                    event.setCancelled(ex.isCancelled());
                }

            } else
                try {
                    victimGame.onPlayerDamage((Player) attacker, victim, event);

                } catch (final EventHandledException ex) {
                    event.setCancelled(ex.isCancelled());
                }

            if (ENTITY_TYPE_MANIPULATION_BLACKLIST.contains(victim.getType().toString()) && !attackerGame.isEdited())
                event.setCancelled(true);
        } else {
            if (victim instanceof Player) {
                final PlayerCache victimCache = PlayerCache.from((Player) victim);

                if (victimCache.getCurrentGameMode() != GameJoinMode.PLAYING) {
                    event.setCancelled(true);

                    return;
                }
            }
            try {
                victimGame.onDamage(attacker, victim, event);

            } catch (final EventHandledException ex) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityCombust(final EntityCombustEvent event) {
        this.cancelIfInGame(event, event.getEntity().getLocation());
    }

    @EventHandler
    public void onEntityInteract(final EntityInteractEvent event) {
        this.cancelIfInStoppedOrLobby(event, event.getEntity());
    }

    @EventHandler
    public void onExplosionPrime(final ExplosionPrimeEvent event) {
        this.cancelIfInStoppedOrLobby(event, event.getEntity());
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        this.preventBlockGrief(event, event.getLocation(), event.blockList());
    }

    private void preventBlockGrief(final Cancellable event, final Location centerLocation, final List<Block> blocks) {
        final Game fromGame = Game.findByLocation(centerLocation);

        if (fromGame != null) {
            try {
                fromGame.onExplosion(centerLocation, blocks, event);

            } catch (final EventHandledException ex) {
                if (ex.isCancelled()) {
                    event.setCancelled(true);

                    return;
                }
            }
        }

        for (final Iterator<Block> it = blocks.iterator(); it.hasNext(); ) {
            final Block block = it.next();
            final Game otherGame = Game.findByLocation(block.getLocation());

            if (otherGame == null && fromGame != null) {
                it.remove();

                continue;
            }

            if (otherGame != null && (!otherGame.hasDestruction() || !otherGame.isPlayed()))
                it.remove();
        }
    }

    @EventHandler
    public void onEntityTarget(final EntityTargetEvent event) {
        final Entity from = event.getEntity();
        final Entity target = event.getTarget();

        final Game fromGame = Game.findByLocation(from.getLocation());

        // Prevent exp from being drawn into players
        if (from instanceof ExperienceOrb && fromGame != null) {
            from.remove();

            return;
        }

        final Game targetGame = target != null ? Game.findByLocation(target.getLocation()) : null;

        if (targetGame != null) {
            if (!targetGame.isPlayed() && !targetGame.isEdited())
                event.setCancelled(true);

            else if (fromGame == null || !fromGame.equals(targetGame))
                event.setCancelled(true);
        }

        if (target instanceof Player) {
            final PlayerCache cache = PlayerCache.from((Player) target);

            // Prevent players in editing or spectating mode from being targeted
            if (cache.hasGame() && cache.getCurrentGameMode() != GameJoinMode.PLAYING)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        final Entity victim = event.getEntity();

        final Game game = Game.findByLocation(victim.getLocation());

        if (game != null) {
            if (!game.isPlayed() && !game.isEdited()) {
                event.setCancelled(true);

                return;
            }

            if (victim instanceof Player) {
                final Player player = (Player) victim;

                final PlayerCache cache = PlayerCache.from(player);
                if (event.getCause() == EntityDamageEvent.DamageCause.WITHER)
                    event.setCancelled(true);
                if (cache.getCurrentGameMode() == GameJoinMode.SPECTATING) {
                    event.setCancelled(true);

                    player.setFireTicks(0);
                    return;
                }
            }


            try {
                game.onDamage(victim, event);

            } catch (final EventHandledException ex) {
                event.setCancelled(ex.isCancelled());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        final LivingEntity victim = event.getEntity();
        final Game game = Game.findByLocation(victim.getLocation());

        if (game != null) {
            final Player killer = victim.getKiller();
            final PlayerCache killerCache = killer != null ? PlayerCache.from(killer) : null;

            event.setDroppedExp(0);
            event.getDrops().clear();

            // If the killer is a player who's playing in the same arena, call the method
            if (killerCache != null && killerCache.getCurrentGameMode() == GameJoinMode.PLAYING && killerCache.getCurrentGame().equals(game)) {
                try {
                    game.onPlayerKill(killer, victim, event);

                } catch (final EventHandledException ex) {
                    // Handled
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        final Game game = Game.findByLocation(event.getLocation());

        if (game != null) {
            if ((!game.isPlayed() && !game.isEdited()) || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemSpawn(final ItemSpawnEvent event) {
        this.cancelIfInStoppedOrLobby(event, event.getLocation());

        if (event.isCancelled())
            return;

        Item entity = event.getEntity();
        Game game = Game.findByLocation(entity.getLocation());

        if (game == null)
            EntityUtil.trackFlying(entity, () -> {
                final Game gameInNewLocation = Game.findByLocation(entity.getLocation());

                if (gameInNewLocation != null)
                    entity.remove();
            });
        else
            try {
                game.onItemSpawn(entity, event);

            } catch (EventHandledException ex) {
                event.setCancelled(ex.isCancelled());
            }
    }

    @EventHandler
    public void onBlockDispense(final BlockDispenseEvent event) {
        this.cancelIfInStoppedOrLobby(event, event.getBlock().getLocation());
    }

    @EventHandler
    public void onBucketFill(final PlayerBucketFillEvent event) {
        preventBucketGrief(event.getBlockClicked().getLocation(), event);
    }

    @EventHandler
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        preventBucketGrief(event.getBlockClicked().getLocation(), event);
    }

    private <T extends PlayerEvent & Cancellable> void preventBucketGrief(final Location location, final T event) {
        final Game game = Game.findByLocation(location);

        if (game != null) {
            final PlayerCache cache = game.findPlayer(event.getPlayer());

            if (cache == null || cache.getCurrentGameMode() != GameJoinMode.EDITING)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockForm(final BlockFormEvent event) {
        this.cancelIfInGame(event, event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockSpread(final BlockSpreadEvent event) {
        this.cancelIfInGame(event, event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        this.cancelIfInGame(event, event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        this.cancelIfInGame(event, event.getBlock().getLocation());
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock))
            this.cancelIfInGame(event, event.getBlock().getLocation());
    }

    @EventHandler
    public void onDoorBreak(final EntityBreakDoorEvent event) {
        this.cancelIfInGame(event, event.getBlock().getLocation());
    }

    @EventHandler
    public void onEggThrow(final PlayerEggThrowEvent event) {
        final Egg egg = event.getEgg();
        final Game game = Game.findByLocation(egg.getLocation());

        if (game != null)
            event.setHatching(false);
    }

    @EventHandler
    public void onPotionSplash(final PotionSplashEvent event) {
        preventProjectileGrief(event.getEntity());
    }

    @EventHandler
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        preventProjectileGrief(event.getEntity());
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        preventProjectileGrief(event.getEntity());
    }

    @EventHandler
    public void onRocketExplosion(final RocketExplosionEvent event) {
        preventProjectileGrief(event.getProjectile());
    }

    private void preventProjectileGrief(final Projectile projectile) {
        final Game game = Game.findByLocation(projectile.getLocation());

        if (game != null) {

            if (!game.isPlayed() && !game.isEdited())
                projectile.remove();

            else if (projectile.getShooter() instanceof Player) {
                final PlayerCache cache = game.findPlayer((Player) projectile.getShooter());

                if (cache == null || cache.getCurrentGameMode() == GameJoinMode.SPECTATING) {
                    projectile.remove();

                    try {
                        if (projectile instanceof Arrow)
                            ((Arrow) projectile).setDamage(0);
                    } catch (final Throwable t) {
                        // Old MC
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHangingPlace(final HangingPlaceEvent event) {
        preventHangingGrief(event.getEntity(), event);
    }

    @EventHandler
    public void onHangingBreak(final HangingBreakEvent event) {
        preventHangingGrief(event.getEntity(), event);
    }

    private void preventHangingGrief(final Entity hanging, final Cancellable event) {
        final Game game = Game.findByLocation(hanging.getLocation());

        if (game != null && !game.isEdited())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPistonExtend(final BlockPistonExtendEvent event) {
        preventPistonMovement(event, event.getBlocks());
    }

    @EventHandler
    public void onPistonRetract(final BlockPistonRetractEvent event) {
        try {
            preventPistonMovement(event, event.getBlocks());
        } catch (final NoSuchMethodError ex) {
            // Old MC lack the event.getBlocks method
        }
    }

    private void preventPistonMovement(final BlockPistonEvent event, List<Block> blocks) {
        final BlockFace direction = event.getDirection();
        final Game pistonGame = Game.findByLocation(event.getBlock().getLocation());

        // Clone the list otherwise it wont work
        blocks = new ArrayList<>(blocks);

        // Calculate blocks ONE step ahed in the push/pull direction
        for (int i = 0; i < blocks.size(); i++) {
            final Block block = blocks.get(i);

            blocks.set(i, block.getRelative(direction));
        }

        for (final Block block : blocks) {
            final Game game = Game.findByLocation(block.getLocation());

            if (game != null && pistonGame == null || game == null && pistonGame != null) {
                event.setCancelled(true);

                break;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        preventBuild(event.getPlayer(), event, false);

        PlayerCache cache = PlayerCache.from(event.getPlayer());
        if (cache.hasGame())
            cache.getCurrentGame().onBlockMine(event);
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        preventBuild(event.getPlayer(), event, true);


    }

    private <T extends BlockEvent & Cancellable> void preventBuild(final Player player, final T event, boolean place) {
        final Game game = Game.findByLocation(event.getBlock().getLocation());

        if (game != null) {
            final PlayerCache gamePlayer = game.findPlayer(player);
            final Block block = event.getBlock();

            if (gamePlayer == null) {
                Messenger.warn(player, "You cannot build unless you do '/game edit' first.");

                event.setCancelled(true);
                return;
            }

            if (gamePlayer.getCurrentGameMode() == GameJoinMode.EDITING)
                return;

            if (gamePlayer.getCurrentGameMode() == GameJoinMode.SPECTATING) {
                event.setCancelled(true);

                return;
            }

            try {
                if (place)
                    game.onBlockPlace(player, block, (BlockPlaceEvent) event);
                else
                    game.onBlockBreak(player, block, (BlockBreakEvent) event);

            } catch (final EventHandledException ex) {
                event.setCancelled(ex.isCancelled());

                if (ex.isCancelled())
                    player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);
        GameJoinMode mode = cache.getCurrentGameMode();

        if (cache.hasGame() && cache.getCurrentGameMode() == GameJoinMode.PLAYING) {
            cache.getCurrentGame().onPlayerDoubleJump(player, event);
        }

        //todo may need to add this back in if double jump does not work, and scrap above
        this.executeIfPlayingGame(event, (pl, ch) -> {
            if (ch.getCurrentGameMode() == GameJoinMode.SPECTATING || ch.isJoining() || ch.isLeaving())
                return;

//            if (pl.isOp() || ch.getCurrentGameMode() == GameJoinMode.SPECTATING) {
//                Messenger.info(pl, "Bypassing flight restriction...");
//
//                return;
//            }

            pl.setFlying(false);
            pl.setAllowFlight(false);

            // Messenger.error(pl, "You cannot fly while playing a game.");
        });
    }

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        preventItemGrief(event, event.getItemDrop());
    }

    private <T extends PlayerEvent & Cancellable> void preventItemGrief(final T event, final Item item) {
        preventItemGrief(event.getPlayer(), event, item);
    }

    private <T extends PlayerEvent & Cancellable> void preventItemGrief(final Player player, final Cancellable event, final Item item) {
        final Game gameAtLocation = Game.findByLocation(item.getLocation());

        if (gameAtLocation == null)
            return;

        if (!gameAtLocation.isEdited() && !gameAtLocation.isPlayed()) {
            event.setCancelled(true);

            return;
        }

        final PlayerCache cache = gameAtLocation.findPlayer(player);

        if (cache == null || cache.getCurrentGameMode() == GameJoinMode.SPECTATING || cache.getCurrentGameMode() == GameJoinMode.PLAYING)
            event.setCancelled(true);

        if (cache != null && cache.getCurrentGameMode() == GameJoinMode.PLAYING && !(event instanceof PlayerDropItemEvent))
            try {
                gameAtLocation.onPlayerPickupItem(player, cache, item);

            } catch (EventHandledException ex) {
                event.setCancelled(ex.isCancelled());
            }
    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame())
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL)
                try {
                    cache.getCurrentGame().onFall(event);

                } catch (EventHandledException ex) {
                }
    }

    @EventHandler
    public void onBlockTeleport(BlockFromToEvent event) {
        final Block block = event.getBlock();
        final SerializedMap moveData = calculateMoveData(block.getLocation(), event.getToBlock().getLocation());
        final Game fromArena = Game.findByName(moveData.getString("from"));

        if (moveData.getBoolean("leaving") || moveData.getBoolean("entering")
                || (fromArena != null && block.getType() == CompMaterial.DRAGON_EGG.getMaterial()))
            event.setCancelled(true);
    }

    private SerializedMap calculateMoveData(final Location from, final Location to) {
        return calculateMoveData(from, to, null);
    }

    private SerializedMap calculateMoveData(final Location from, final Location to, @Nullable final Player fromPlayer) {
        final Game gameFrom = fromPlayer != null ? PlayerCache.from(fromPlayer).getCurrentGame() : Game.findByLocation(from);
        final Game gameTo = Game.findByLocation(to);

        final boolean isLeaving = gameFrom != null && (gameTo == null || !gameFrom.equals(gameTo));
        final boolean isEntering = gameTo != null && (gameFrom == null || !gameTo.equals(gameFrom));

        return SerializedMap.ofArray(
                "to", gameTo != null ? gameTo.getName() : "",
                "from", gameFrom != null ? gameFrom.getName() : "",
                "toIsArena", gameTo != null,
                "fromIsArena", gameFrom != null,
                "leaving", isLeaving,
                "entering", isEntering);
    }

    private void cancelIfInGame(Cancellable event, Location location) {
        Game game = Game.findByLocation(location);

        if (game != null)
            event.setCancelled(true);
    }

    private void cancelIfInStoppedOrLobby(Cancellable event, Entity entity) {
        this.cancelIfInStoppedOrLobby(event, entity.getLocation());
    }

    private void cancelIfInStoppedOrLobby(Cancellable event, Location location) {
        Game game = Game.findByLocation(location);

        if (game != null && !game.isPlayed() && !game.isEdited())
            event.setCancelled(true);
    }

    private void executeIfPlayingGame(PlayerEvent event, BiConsumer<Player, PlayerCache> consumer) {
        Player player = event.getPlayer();
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame() && cache.getCurrentGameMode() != GameJoinMode.EDITING)
            consumer.accept(player, cache);
    }

    // ------–------–------–------–------–------–------–------–------–------–------–------–
    // Game Specific Events
    // ------–------–------–------–------–------–------–------–------–------–------–------–

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileHitInGame(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof LivingEntity))
            return;
        final Game gameAtLocation = Game.findByLocation(event.getHitEntity().getLocation());
        if (gameAtLocation == null)
            return;
        if (!gameAtLocation.isEdited() && !gameAtLocation.isPlayed()) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = ((Player) event.getEntity().getShooter()).getPlayer();
            PlayerCache cache = PlayerCache.from(shooter);
            Kits shooterKit = PlayerCache.from(shooter).getCurrentKit();

            if (cache.hasGame() && cache.getCurrentGame().getState() == GameState.PREPLAYED)
                event.setCancelled(true);

            if (cache.hasGame() && cache.getCurrentGame().isPlayed() && !cache.getCurrentGame().isStopped())
                try {
                    cache.getCurrentGame().onProjectileHit(event);

                } catch (EventHandledException ex) {
                }
        }
    }
    
    @EventHandler
    public void onEntityDamageSSM(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        final Game gameAtLocation = Game.findByLocation(event.getEntity().getLocation());

        //Protection from Creeper Kit
        if (event.getDamager() instanceof LightningStrike) {
            event.setCancelled(true);
            return;
        }
        if (gameAtLocation == null || !(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
            return;

        if (!gameAtLocation.isEdited() && !gameAtLocation.isPlayed()) {
            event.setCancelled(true);
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        final PlayerCache attackerCache = gameAtLocation.findPlayer(attacker);
        final PlayerCache victimCache = gameAtLocation.findPlayer(victim);

        if (attackerCache == null || attackerCache.getCurrentGameMode() == GameJoinMode.SPECTATING ||
                victimCache == null || victimCache.getCurrentGameMode() == GameJoinMode.SPECTATING)
            event.setCancelled(true);

        if (attackerCache != null && attackerCache.getCurrentGameMode() == GameJoinMode.PLAYING && attackerCache.getCurrentGame() instanceof SSMCore &&
                victimCache != null || victimCache.getCurrentGameMode() == GameJoinMode.PLAYING && victimCache.getCurrentGame() instanceof SSMCore) {
            Kits attackersKit = attackerCache.getCurrentKit();
            Kits victimsKit = victimCache.getCurrentKit();

            if (victimsKit instanceof org.mammothplugins.ssm.model.ssm.kits.Creeper)
                ((org.mammothplugins.ssm.model.ssm.kits.Creeper) victimsKit).onMeleeAttack(attacker);
            if (victimsKit instanceof org.mammothplugins.ssm.model.ssm.kits.Blaze)
                if (((org.mammothplugins.ssm.model.ssm.kits.Blaze) victimsKit).isLaunching(victim))
                    ((org.mammothplugins.ssm.model.ssm.kits.Blaze) victimsKit).setIsHitInLaunch(true);
        }
    }

    @EventHandler
    public void onEntityInteractSSM(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        final Game gameAtLocation = Game.findByLocation(player.getLocation());
        if (gameAtLocation == null)
            return;
        if (!gameAtLocation.isEdited() && !gameAtLocation.isPlayed()) {
            event.setCancelled(true);
            return;
        }
        final PlayerCache cache = gameAtLocation.findPlayer(player);

        if (cache == null || cache.getCurrentGameMode() == GameJoinMode.SPECTATING)
            event.setCancelled(true);

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            if (cache != null || cache.getCurrentGameMode() == GameJoinMode.PLAYING && cache.getCurrentGame() instanceof SSMCore) {
                Kits kit = cache.getCurrentKit();

                if (kit instanceof org.mammothplugins.ssm.model.ssm.kits.WitherSkeleton) {
                    if (player.getItemInHand().getType() == CompMaterial.IRON_SWORD.toItem().getType())
                        ((org.mammothplugins.ssm.model.ssm.kits.WitherSkeleton) kit).onSwordGuide(player);
                    if (player.getItemInHand().getType() == CompMaterial.IRON_AXE.toItem().getType())
                        ((org.mammothplugins.ssm.model.ssm.kits.WitherSkeleton) kit).swap(player);
                }
            }
    }

    @EventHandler
    public void onEntityTargetSSM(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.WitherSkeleton) || !(event.getTarget() instanceof Player))
            return;
        org.bukkit.entity.WitherSkeleton image = (org.bukkit.entity.WitherSkeleton) event.getEntity();
        Player player = (Player) event.getTarget();

        final Game gameAtLocation = Game.findByLocation(player.getLocation());
        if (gameAtLocation == null)
            return;
        if (!gameAtLocation.isEdited() && !gameAtLocation.isPlayed()) {
            event.setCancelled(true);
            return;
        }
        final PlayerCache cache = gameAtLocation.findPlayer(player);
        if (cache == null || cache.getCurrentGameMode() == GameJoinMode.SPECTATING)
            event.setCancelled(true);

        if (cache != null || cache.getCurrentGameMode() == GameJoinMode.PLAYING && cache.getCurrentGame() instanceof SSMCore) {
            Kits kit = cache.getCurrentKit();
            if (kit instanceof org.mammothplugins.ssm.model.ssm.kits.WitherSkeleton) //Stops the WitherSkeleton from attacking it's Kit Owner
                event.setCancelled(true);

        }
    }


    @EventHandler
    public void onPlayerCollideWithPlayer(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        final Game gameAtLocation = Game.findByLocation(player.getLocation());
        if (gameAtLocation == null)
            return;
        if (!gameAtLocation.isEdited() && !gameAtLocation.isPlayed())
            return;
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasGame() && cache.getCurrentGame().isPlayed())
            try {
                if (player.getNearbyEntities(1, 1, 1).size() >= 1) {
                    for (Entity entity : player.getNearbyEntities(1, 1, 1))
                        if (entity instanceof Player)
                            cache.getCurrentGame().onPlayerCollideWithOtherPlayerEvent(event, (Player) entity);
                }

            } catch (EventHandledException ex) {
            }
    }

// ------–------–------–------–------–------–------–------–------–------–------–------–
// Custom classes for compatibility reasons
// ------–------–------–------–------–------–------–------–------–------–------–------–

    private class EntityBreedListener implements Listener {
        @EventHandler
        public void onEntityBreed(final EntityBreedEvent event) {
            cancelIfInStoppedOrLobby(event, event.getEntity());
        }
    }

    private class SpawnerSpawnListener implements Listener {
        @EventHandler
        public void onSpawnerSpawn(final SpawnerSpawnEvent event) {
            cancelIfInStoppedOrLobby(event, event.getEntity());
        }
    }

    private class ExplodeAndEntitySpawnListener implements Listener {

        @EventHandler
        public void onBlockExplode(final BlockExplodeEvent event) {
            preventBlockGrief(event, event.getBlock().getLocation(), event.blockList());
        }

        @EventHandler
        public void onEntitySpawn(final EntitySpawnEvent event) {

            final Entity entity = event.getEntity();
            final Game arena = Game.findByLocation(event.getLocation());

            if (arena == null)
                return;

            if (!arena.isPlayed() && !arena.isEdited()) {
                event.setCancelled(true);

                return;
            }

            if (ENTITY_TYPE_MANIPULATION_BLACKLIST.contains(entity.getType().toString()) && !arena.isEdited())
                event.setCancelled(true);

            // Track flying blocks and remove them once arena border is reached
            if (entity instanceof FallingBlock)
                arena.preventLeave((FallingBlock) entity);
        }
    }

    private class ConsumeItemListener implements Listener {

        @EventHandler
        public void onConsumeFood(final PlayerItemConsumeEvent event) {
            cancelIfInStoppedOrLobby(event, event.getPlayer());
        }
    }

    private class BlockReceiveGameListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onSculkActivate(BlockReceiveGameEvent event) {
            cancelIfInGame(event, event.getBlock().getLocation());
        }
    }

    private class CauldronLevelChangeListener implements Listener {
        @EventHandler
        public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
            cancelIfInGame(event, event.getBlock().getLocation());
        }
    }

    private class EntityEnterBlockListener implements Listener {

        @EventHandler
        public void onEntityEnterBlock(EntityEnterBlockEvent event) {
            cancelIfInStoppedOrLobby(event, event.getBlock().getLocation());
        }
    }

    private class EntityPickupItemListener implements Listener {

        @EventHandler
        public void onEntityPickupItem(EntityPickupItemEvent event) {

            if (event.getEntity() instanceof Player)
                preventItemGrief((Player) event.getEntity(), event, event.getItem());

            else
                cancelIfInStoppedOrLobby(event, event.getItem());
        }
    }

    private class PlayerPickupItemListener implements Listener {

        @EventHandler
        public void onItemPickup(final PlayerPickupItemEvent event) {
            preventItemGrief(event, event.getItem());
        }
    }

    private class PlayerBucketEntityListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerBucket(PlayerBucketEntityEvent event) {
            cancelIfInStoppedOrLobby(event, event.getEntity());
        }
    }

    private class PlayerBucketFishListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerFish(PlayerBucketFishEvent event) {
            cancelIfInStoppedOrLobby(event, event.getEntity());
        }
    }

    private class PlayerTakeLecternBookListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerTakeLectern(PlayerTakeLecternBookEvent event) {
            cancelIfInGame(event, event.getLectern().getLocation());
        }
    }

    private class RaidTriggerListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onRaidTrigger(RaidTriggerEvent event) {
            cancelIfInGame(event, event.getRaid().getLocation());
        }
    }
}
