package org.mammothplugins.baconBrawl.model;

import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mammothplugins.baconBrawl.settings.Settings;
import org.mammothplugins.baconBrawl.tool.SpectatePlayersTool;
import org.mineacademy.fo.*;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.exception.EventHandledException;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.model.*;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.ConfigItems;
import org.mineacademy.fo.settings.SimpleSettings;
import org.mineacademy.fo.settings.YamlConfig;
import org.mineacademy.fo.visual.VisualizedRegion;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/*
Default Game Template
 */
public abstract class Game extends YamlConfig {

    private static final String FOLDER = "games";

    public static final String TAG_TELEPORTING = "Game_Teleporting";

    private static final ConfigItems<? extends Game> loadedFiles = ConfigItems.fromFolder(FOLDER, fileName -> {
        final YamlConfig config = YamlConfig.fromFileFast(FileUtil.getFile(FOLDER + "/" + fileName + ".yml"));
        final GameType type = config.get("Type", GameType.class);

        Valid.checkNotNull(type, "Unrecognized GameType." + config.getObject("Type") + " in " + fileName + "! Available: " + Common.join(GameType.values()));
        return type.getInstanceClass();
    });

    private GameType type;

    private int minPlayers;
    private int maxPlayers;
    private VisualizedRegion region;
    private Location lobbyLocation;
    private Location deathSpawnLocation;
    private Location returnBackLocation;
    private SimpleTime lobbyDuration;
    private SimpleTime gameDuration;
    private boolean destruction;
    private boolean restoreWorld;

    /* ------------------------------------------------------------------------------- */

    private final StrictList<PlayerCache> players = new StrictList<>();
    private final StrictMap<Location, BlockState> placedBlocks = new StrictMap<>();

    private Countdown startCountdown;
    private Countdown heartbeat;
    private GameScoreboard scoreboard;
    private GameState state = GameState.STOPPED;
    private boolean stopping;
    private boolean starting;

    protected Game(String name) {
        this(name, null);
    }

    protected Game(String name, @Nullable GameType type) {
        this.type = type;

        this.setHeader(
                Common.configLine(),
                "This file stores information about a single game.",
                Common.configLine() + "\n");

        this.loadConfiguration(NO_DEFAULT, FOLDER + "/" + name + ".yml");

        this.startCountdown = this.compileStartCountdown();
        this.heartbeat = this.compileHeartbeat();
        this.scoreboard = this.compileScoreboard();
    }

    protected Countdown compileStartCountdown() {
        return new GameCountdownStart(this);
    }

    protected GameHeartbeat compileHeartbeat() {
        return new GameHeartbeat(this);
    }

    protected GameScoreboard compileScoreboard() {
        return new GameScoreboard(this);
    }

    @Override
    protected void onLoad() {
        this.minPlayers = getInteger("Min_Players", 1);
        this.maxPlayers = getInteger("Max_Players", 10);
        this.region = get("Region", VisualizedRegion.class, new VisualizedRegion());
        this.lobbyLocation = getLocation("Lobby_Location");
        this.deathSpawnLocation = getLocation("Death_Spawnpoint_Location");
        this.returnBackLocation = getLocation("Return_Back_Location");
        this.lobbyDuration = getTime("Lobby_Duration", SimpleTime.from("15 seconds"));
        this.gameDuration = getTime("Game_Duration", SimpleTime.from("20 minutes"));
        this.destruction = getBoolean("Destruction", false);
        this.restoreWorld = getBoolean("Restore_World", false);

        if (this.type != null)
            this.save();

        else
            this.type = get("Type", GameType.class);
    }

    @Override
    protected void onSave() {
        this.set("Type", this.type);
        this.set("Min_Players", this.minPlayers);
        this.set("Max_Players", this.maxPlayers);
        this.set("Region", this.region);
        this.set("Lobby_Location", this.lobbyLocation);
        this.set("Death_Spawnpoint_Location", this.deathSpawnLocation);
        this.set("Return_Back_Location", this.returnBackLocation);
        this.set("Lobby_Duration", this.lobbyDuration);
        this.set("Game_Duration", this.gameDuration);
        this.set("Destruction", this.destruction);
        this.set("Restore_World", this.restoreWorld);
    }

    /* ------------------------------------------------------------------------------- */

    public final GameType getType() {
        return type;
    }

    public final int getMinPlayers() {
        return minPlayers;
    }

    public final void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;

        this.save();
    }

    public final int getMaxPlayers() {
        return maxPlayers;
    }

    public final void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;

        this.save();
    }

    public final VisualizedRegion getRegion() {
        return region;
    }

    public final void setRegion(final Location primary, final Location secondary) {
        this.region.updateLocation(primary, secondary);

        this.save();
    }

    public final Location getLobbyLocation() {
        return this.lobbyLocation;
    }

    public final void setLobbyLocation(final Location location) {
        this.lobbyLocation = location;

        this.save();
    }

    public final Location getDeathSpawnLocation() {
        return this.deathSpawnLocation;
    }

    public final void setDeathSpawnLocation(final Location location) {
        this.deathSpawnLocation = location;

        this.save();
    }

    public final Location getReturnBackLocation() {
        return returnBackLocation;
    }

    public void setReturnBackLocation(Location returnBackLocation) {
        this.returnBackLocation = returnBackLocation;

        this.save();
    }

    public final SimpleTime getLobbyDuration() {
        return lobbyDuration;
    }

    public final SimpleTime getGameDuration() {
        return this.gameDuration;
    }

    public boolean isSetup() {
        return this.region.isWhole() && this.lobbyLocation != null && this.deathSpawnLocation != null;
    }

    public final Countdown getStartCountdown() {
        return this.startCountdown;
    }

    public final Countdown getHeartbeat() {
        return this.heartbeat;
    }

    public final GameScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public final GameState getState() {
        return this.state;
    }

    public final boolean isStarting() {
        return this.starting;
    }

    public final boolean isStopping() {
        return this.stopping;
    }

    public abstract Location getRespawnLocation(Player player);

    public boolean hasPvP() {
        return true;
    }

    public boolean hasDestruction() {
        return this.destruction;
    }

    public boolean canRestoreRegion() {
        return HookManager.isWorldEditLoaded() && MinecraftVersion.atLeast(MinecraftVersion.V.v1_13);
    }

    public final boolean isStopped() {
        return this.state == GameState.STOPPED;
    }

    public final boolean isEdited() {
        return this.state == GameState.EDITED;
    }

    public final boolean isPlayed() {
        return this.state == GameState.PLAYED;
    }

    public final boolean isLobby() {
        return this.state == GameState.LOBBY;
    }

    @Override
    public final String getName() {
        return super.getName();
    }

    /* ------------------------------------------------------------------------------- */

    public final void start() {
        Valid.checkBoolean(this.state == GameState.LOBBY, "Cannot start game " + this.getName() + " while in the " + this.state + " mode");

        this.state = GameState.PLAYED;
        this.starting = true;

        try {

            if (this.players.size() < this.minPlayers) {
                this.stop(GameStopReason.NOT_ENOUGH_PLAYERS);

                return;
            }

            this.cleanEntities();
            this.heartbeat.launch();
            this.scoreboard.onGameStart();

            // /game forcestart --> bypass lobby waiting
            if (this.startCountdown.isRunning())
                this.startCountdown.cancel();

            try {
                this.onGameStart();

            } catch (final Throwable t) {
                Common.error(t, "Failed to start game " + this.getName() + ", stopping for safety");

                this.stop(GameStopReason.ERROR);
            }

            this.forEachInAllModes(cache -> {
                Player player = cache.toPlayer();

                // Close all players inventories
                player.closeInventory();

                this.onGameStartFor(player, cache);
            });

            try {
                this.onGamePostStart();

            } catch (final Throwable t) {
                Common.error(t, "Failed to start game " + this.getName() + ", stopping for safety");

                this.stop(GameStopReason.ERROR);
            }

            this.broadcastInfo("Game " + this.getName() + " starts now! Players: " + this.players.size());
            Common.log("Started game " + this.getName());

        } finally {
            this.starting = false;
        }
    }

    public final void stop(GameStopReason stopReason) {
        Valid.checkBoolean(this.state != GameState.STOPPED, "Cannot stop stopped game " + this.getName());

        try {
            this.stopping = true;

            if (this.startCountdown.isRunning())
                this.startCountdown.cancel();

            if (this.heartbeat.isRunning())
                this.heartbeat.cancel();

            final int playingPlayersCount = this.getPlayers(GameJoinMode.PLAYING).size();

            try {
                this.onGameStop();
            } catch (final Throwable t) {
                Common.error(t, "Failed to properly stop game " + this.getName());
            }
            this.forEachPlayerInAllModes(player -> {

                String stopMessage = stopReason.getMessage();

                if (stopMessage != null) {
                    BoxedMessage.tell(player, "<center>&c&lGAME OVER\n\n<center>&7"
                            + Replacer.replaceArray(stopMessage,
                            "game", this.getName(),
                            "players", playingPlayersCount,
                            "min_players", this.getMinPlayers()).replace("\n", "\n<center>&7"));
                } else if (PlayerCache.from(player).getCurrentGameMode() == GameJoinMode.SPECTATING)
                    BoxedMessage.tell(player, "<center>&c&lGAME OVER\n\n" +
                            "<center>&7This game has been ended.\n" +
                            "<center>&7Thank you for spectating.");
                this.leavePlayer(player, GameLeaveReason.GAME_STOP, false);

            });

            this.scoreboard.onGameStop();
            this.cleanEntities();

            for (BlockState oldBlockStates : this.placedBlocks.values())
                oldBlockStates.update(true);

            this.placedBlocks.clear();

            if (this.destruction) {
                if (this.restoreWorld)
                    GameWorldManager.restoreWorld(this);

                else if (this.canRestoreRegion())
                    GameRegionManager.restoreRegion(this);
            }

        } finally {
            this.state = GameState.STOPPED;
            this.players.clear();
            this.stopping = false;

            Common.log("Stopped game " + this.getName());
        }
    }

    private void cleanEntities() {

        if (!this.region.isWhole())
            return;

        final Set<String> ignoredEntities = Common.newSet("PLAYER", "ITEM_FRAME", "PAINTING", "ARMOR_STAND", "LEASH_HITCH", "ENDER_CRYSTAL");

        for (final Entity entity : this.region.getEntities())
            if (!ignoredEntities.contains(entity.getType().toString()))
                entity.remove();
    }

    // ------–------–------–------–------–------–------–------–------–------–------–------–
    public final boolean joinPlayer(final Player player, final GameJoinMode mode) {
        final PlayerCache cache = PlayerCache.from(player);

        if (!this.canJoin(player, mode))
            return false;

        cache.setJoining(true);

        try {

            cache.clearTags();

            if (mode != GameJoinMode.EDITING) {

                cache.setPlayerTag("PreviousLocation", player.getLocation());

                PlayerUtil.storeState(player);

                this.teleport(player, this.lobbyLocation);

                cache.setPlayerTag("AllowGamemodeChange", true);
                PlayerUtil.normalize(player, true);
                cache.removePlayerTag("AllowGamemodeChange");
            }

            try {
                this.onGameJoin(player, mode);

            } catch (final Throwable t) {
                Common.error(t, "Failed to properly handle " + player.getName() + " joining to game " + this.getName() + ", aborting");

                return false;
            }

            cache.setCurrentGameMode(mode);
            cache.setCurrentGameName(this.getName());

            this.players.add(cache);

            // Start countdown and change game mode
            if (this.state == GameState.STOPPED)
                if (mode == GameJoinMode.EDITING) {
                    Valid.checkBoolean(!this.startCountdown.isRunning(), "Game start countdown already running for " + getName());

                    this.state = GameState.EDITED;
                    this.scoreboard.onEditStart();

                    player.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false); //todo added no weather change here
                    player.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                    this.onGameEditStart();

                } else {
                    Valid.checkBoolean(!this.startCountdown.isRunning(), "Game start countdown already running for " + this.getName());

                    this.state = GameState.LOBBY;
                    this.scoreboard.onLobbyStart();

                    if (!Settings.AutoMode.ENABLED)
                        this.startCountdown.launch();

                    this.onGameLobbyStart();

                    if (this.destruction) {
                        if (this.restoreWorld)
                            GameWorldManager.disableAutoSave(this);

                        else if (this.canRestoreRegion())
                            GameRegionManager.saveRegion(this);
                    }
                }

            Messenger.success(player, "You are now " + mode.toString().toLowerCase() + " game '" + this.getName() + "'!");

            if (this.isLobby()) {
                this.broadcast("&6" + player.getName() + " &7has joined the game! (" + this.players.size() + "/" + this.maxPlayers + ")");

                if (this.getPlayers(GameJoinMode.PLAYING).size() >= this.getMinPlayers() && Settings.AutoMode.ENABLED) {
                    this.startCountdown.launch();

                    this.forEachPlayerInAllModes(otherPlayer -> Remain.sendTitle(otherPlayer,
                            "",
                            "&eGame Starts In " + Common.plural(this.startCountdown.getTimeLeft(), "second")));
                }
            }

            this.scoreboard.onPlayerJoin(player);

            if (mode == GameJoinMode.SPECTATING)
                this.transformToSpectate(player);

            this.checkIntegrity();

        } finally {
            cache.setJoining(false);
        }

        return true;
    }

    public final boolean canJoin(Player player, GameJoinMode mode) {
        final PlayerCache cache = PlayerCache.from(player);

        if (cache.getCurrentGame() != null) {
            Messenger.error(player, "You are already joined in game '" + cache.getCurrentGameName() + "'.");

            return false;
        }

        // Perhaps admins joining another player into an game?
        if (player.isDead()) {
            if (Settings.AutoMode.ENABLED)
                Remain.respawn(player);

            else {
                Messenger.error(player, "You cannot join game '" + this.getName() + "' while you are dead.");

                return false;
            }
        }

        if (!Settings.AutoMode.ENABLED) {
            if (mode != GameJoinMode.EDITING && (!player.isOnGround() || player.getFallDistance() > 0)) {
                Messenger.error(player, "You cannot join game '" + this.getName() + "' while you are flying.");

                return false;
            }

            if (mode != GameJoinMode.EDITING && player.getFireTicks() > 0) {
                Messenger.error(player, "You cannot join game '" + this.getName() + "' while you are burning.");

                return false;
            }
        }

        if (this.state == GameState.EDITED && mode != GameJoinMode.EDITING) {
            Messenger.error(player, "You cannot join game '" + this.getName() + "' for play while it is being edited.");

            return false;
        }

        if ((this.state == GameState.LOBBY || this.state == GameState.PLAYED) && mode == GameJoinMode.EDITING) {
            Messenger.error(player, "You edit game '" + this.getName() + "' for play while it is being played.");

            return false;
        }

        if (this.state != GameState.PLAYED && mode == GameJoinMode.SPECTATING) {
            Messenger.error(player, "Only games that are being played may be spectated.");

            return false;
        }

        if (this.state == GameState.PLAYED && mode == GameJoinMode.PLAYING) {
            Messenger.error(player, "This game has already started. Type '/game spectate " + this.getName() + "' to observe.");

            return false;
        }

        if (!this.isSetup() && mode != GameJoinMode.EDITING) {
            Messenger.error(player, "Game " + this.getName() + " is not yet configured. If you are an admin, run '/game edit " + this.getName() + "' to see what's missing.");

            return false;
        }

        if (mode == GameJoinMode.PLAYING && this.players.size() >= this.getMaxPlayers()) {
            Messenger.error(player, "Game '" + this.getName() + "' is full (" + this.getMaxPlayers() + " players)!");

            return false;
        }

        if (GameWorldManager.isWorldBeingProcessed(this.region.getWorld())) {
            Messenger.error(player, "This game is processed right now.");

            return false;
        }

        return true;
    }

    public final void leavePlayer(Player player, GameLeaveReason leaveReason) {
        this.leavePlayer(player, leaveReason, true);
    }

    private void leavePlayer(Player player, GameLeaveReason leaveReason, boolean stopIfLast) {
        final PlayerCache cache = PlayerCache.from(player);

        Valid.checkBoolean(!this.isStopped(), "Cannot leave player " + player.getName() + " from stopped game!");
        Valid.checkBoolean(cache.hasGame() && cache.getCurrentGame().equals(this), "Player " + player.getName() + " is not joined in game " + this.getName());

        cache.setLeaving(true);

        if (this.getPlayers(GameJoinMode.PLAYING).size() > 0 && leaveReason.autoSpectateOnLeave() && this.canSpectateOnLeave(player)) {
            cache.setLeaving(false);

            this.transformToSpectate(player);
            this.onGameSpectate(player);
            this.broadcast("&6" + player.getName() + " &7has lost the game and is now spectating!");

        } else {

            try {
                this.scoreboard.onPlayerLeave(player);

                // If onLeave uses players then move it below because this player will be removed at the point of calling onLeave!
                this.players.remove(cache);

                try {
                    this.onGameLeave(player);

                } catch (final Throwable t) {
                    Common.error(t, "Failed to properly handle " + player.getName() + " leaving game " + this.getName() + ", stopping for safety");

                    if (!this.isStopped()) {
                        stop(GameStopReason.ERROR);

                        return;
                    }
                }

                if (!this.isEdited()) {
                    final Location previousLocation = cache.getPlayerTag("PreviousLocation");
                    Valid.checkNotNull(previousLocation, "Unable to locate previous location for player " + player.getName());

                    PlayerUtil.normalize(player, true);

                    Location respawnLocation = previousLocation;

                    if (Game.findByLocation(previousLocation) != null && this.returnBackLocation != null)
                        respawnLocation = this.returnBackLocation;

                    this.teleportToReturnLocation(player, respawnLocation);

                    Common.runLater(2, () -> {
                        cache.setPlayerTag("AllowGamemodeChange", true);
                        PlayerUtil.restoreState(player);
                        cache.removePlayerTag("AllowGamemodeChange");
                    });
                }

                // If we are not stopping, remove from the map automatically
                if (this.getPlayers(GameJoinMode.PLAYING).isEmpty() && stopIfLast)
                    this.stop(GameStopReason.LAST_PLAYER_LEFT);

                if (!this.stopping)
                    Messenger.success(player, "You've left " + cache.getCurrentGameMode().toString().toLowerCase() + " the game '" + this.getName() + "'!");

                if (cache.getCurrentGameMode() != GameJoinMode.SPECTATING)
                    this.broadcast("&6" + player.getName() + " &7has left the game! (" + this.getPlayers(GameJoinMode.PLAYING).size() + "/" + this.maxPlayers + ")");

            } finally {
                cache.setLeaving(false);
                cache.setCurrentGameMode(null);
                cache.setCurrentGameName(null);
                cache.clearTags();
            }
        }
    }

    public final void teleportToReturnLocation(Player player) {
        this.teleportToReturnLocation(player, Common.getOrDefault(this.returnBackLocation, player.getWorld().getSpawnLocation()));
    }

    private void teleportToReturnLocation(Player player, @NonNull Location location) {
        PlayerCache cache = PlayerCache.from(player);

        if (cache.hasPlayerTag("SwitchToEditing")) {
            cache.removePlayerTag("SwitchToEditing");

            return;
        }

        if (Settings.AutoMode.ENABLED)
            BungeeUtil.connect(player, Settings.AutoMode.RETURN_BACK_SERVER);
        else
            this.teleport(player, location);
    }

    protected boolean canSpectateOnLeave(Player player) {
        return this.getPlayers(GameJoinMode.PLAYING).size() > 1;
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

        this.scoreboard.onSpectateStart();
    }

    /* ------------------------------------------------------------------------------- */
    /* Overridable methods */
    /* ------------------------------------------------------------------------------- */

    protected void onGameJoin(Player player, GameJoinMode mode) {
    }

    protected void onGameSpectate(Player player) {
    }

    protected void onGameLeave(Player player) {
    }

    protected void onGameEditStart() {
    }

    protected void onGameLobbyStart() {
    }

    protected void onGameStart() {
    }

    protected void onGamePostStart() {
    }

    protected void onGameStartFor(Player player, PlayerCache cache) {
    }

    protected void onGameStop() {
    }

    public void onPlayerChat(PlayerCache cache, AsyncPlayerChatEvent event) throws EventHandledException {
        final List<Player> gamePlayers = cache.getCurrentGame().getBukkitPlayersInAllModes();

        if (cache.getCurrentGameMode() == GameJoinMode.EDITING)
            this.returnHandled();

        event.getRecipients().removeIf(recipient -> !gamePlayers.contains(recipient));
        event.setFormat(Common.colorize("&8[&6" + cache.getCurrentGameName() + "&8] &f" + cache.getPlayerName() + "&8: &f" + event.getMessage()));
    }

    public void onPlayerDeath(PlayerCache cache, PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Instantly skip Death Screen and respawn the player
        if (this.isLobby() || cache.getCurrentGameMode() == GameJoinMode.SPECTATING || this.canRespawn(player, cache))
            Remain.respawn(player);

        event.setDeathMessage(null);

        try {
            event.setKeepInventory(true);

        } catch (NoSuchMethodError err) {
            // Ancient MC versions
        }
    }

    protected boolean canRespawn(Player player, PlayerCache cache) {
        return true;
    }

    public void onPlayerRespawn(PlayerCache cache, PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (this.state == GameState.LOBBY) {
            event.setRespawnLocation(this.getLobbyLocation());

            return;
        }

        if (cache.getCurrentGameMode() == GameJoinMode.SPECTATING)
            this.transformToSpectate(player);

        event.setRespawnLocation(getDeathSpawnLocation());
    }

    public void onPlayerMeleeAttack(EntityDamageByEntityEvent event, Player damager) {

    }

    public void onCooldown(PlayerInteractEvent event) {
    }

    public void onFall(EntityDamageEvent event) {

    }

    public void onProjectileHit(ProjectileHitEvent event) {

    }

    public void onPlayerCommand(PlayerCache cache, PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        GameJoinMode mode = cache.getCurrentGameMode();

        if (mode == GameJoinMode.EDITING || player.isOp())
            returnHandled();

        final String label = event.getMessage().split(" ")[0];
        this.checkBoolean(label.equals("/#flp")
                || Valid.isInList(label, SimpleSettings.MAIN_COMMAND_ALIASES), player, "You cannot execute this command while playing.");
    }

    public void onPlayerInteractItem(ItemStack item, PlayerInteractEvent event) {
    }

    public void onPlayerInteract(PlayerCache cache, PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final GameJoinMode mode = cache.getCurrentGameMode();

        if (mode == GameJoinMode.EDITING)
            this.returnHandled();

        if (mode == GameJoinMode.SPECTATING)
            this.cancelEvent();

        final boolean isBoat = event.hasItem() && CompMaterial.isBoat(event.getItem().getType());
        final boolean isSoil = event.hasBlock() && event.getClickedBlock().getType() == CompMaterial.FARMLAND.getMaterial();

        if (isBoat || isSoil) {
            player.updateInventory();

            this.cancelEvent();
        }
    }

    public void onPvP(Player attacker, Player victim, EntityDamageByEntityEvent event) {
    }

    public void onPlayerDamage(Player attacker, Entity victim, EntityDamageByEntityEvent event) {
    }

    public void onDamage(Entity attacker, Entity victim, EntityDamageByEntityEvent event) {
    }

    public void onDamage(Entity victim, EntityDamageEvent event) {
    }

    public void onPlayerDoubleJump(Player player, PlayerToggleFlightEvent event) {
    }

    public void onPlayerLand(Player player, PlayerMoveEvent event) {
    }

    public void onExplosion(Location centerLocation, List<Block> blocks, Cancellable event) {
        if (!this.isPlayed())
            cancelEvent();

        if (event instanceof EntityExplodeEvent)
            ((EntityExplodeEvent) event).setYield(0F);

        try {
            if (event instanceof BlockExplodeEvent) // 1.8.8
                ((BlockExplodeEvent) event).setYield(0F);

        } catch (final Throwable t) {
            // Old MC
        }
    }

    public void onPlayerKill(Player killer, LivingEntity victim, EntityDeathEvent event) {
    }

    public void onPlayerPickupItem(Player player, PlayerCache cache, Item item) {
    }

    public void onFall(Player player, EntityDamageEvent event) {
    }

    public void onBlockPlace(Player player, Block block, BlockPlaceEvent event) {

        if (CompMetadata.hasMetadata(event.getItemInHand(), "PlaceableItem")) {
            this.placedBlocks.put(block.getLocation(), event.getBlockReplacedState());
            return;
        }

        this.cancelEvent();
    }

    public void onBlockBreak(Player player, Block block, BlockBreakEvent event) {
        this.cancelEvent();
    }

    protected final void handleCustomBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        BlockState placedBlock = this.placedBlocks.get(location);

        if (placedBlock != null) {
            this.placedBlocks.remove(location);

            ItemCreator
                    .of(CompMaterial.fromBlock(block))
                    .tag("PlaceableItem", "true")
                    .drop(location);

            block.setType(Material.AIR);
            this.cancelEvent();
        }
    }

    public void onEntityClick(Player player, Entity entity, PlayerInteractEntityEvent event) {
        if (entity instanceof ItemFrame)
            this.cancelEvent();
    }

    public void onItemSpawn(Item item, ItemSpawnEvent event) {
    }

    public void onBedEnter(PlayerCache cache, PlayerBedEnterEvent event) {
        this.cancelEvent();
    }

    public void onPlayerInventoryClick(PlayerCache cache, InventoryClickEvent event) {
        if (!this.isPlayed())
            this.cancelEvent();
    }

    public void onPlayerInventoryDrag(PlayerCache cache, InventoryDragEvent event) {
        if (!this.isPlayed())
            this.cancelEvent();
    }

    public void onPlayerMoveEvent(PlayerMoveEvent event) {
    }

    /* ------------------------------------------------------------------------------- */

    public final void broadcastInfo(final String message) {
        this.checkIntegrity();

        this.forEachPlayerInAllModes(player -> Messenger.info(player, message));
    }

    public final void broadcastWarn(final String message) {
        this.checkIntegrity();

        this.forEachPlayerInAllModes(player -> Messenger.warn(player, message));
    }

    public final void broadcast(final String message) {
        this.checkIntegrity();

        this.forEachPlayerInAllModes(player -> Common.tellNoPrefix(player, message));
    }

    public final void preventLeave(FallingBlock falling) {
        final Runnable tracker = () -> {
            final Location newLocation = falling.getLocation();
            final Game newArena = Game.findByLocation(newLocation);

            if (newArena == null || !newArena.equals(this))
                falling.remove();
        };

        EntityUtil.track(falling, 10 * 20, tracker, tracker);
    }

    public final void teleport(final Player player, @NonNull final Location location) {
        Valid.checkBoolean(player != null && player.isOnline(), "Cannot teleport offline players!");

        final Location topOfTheBlock = location.getBlock().getLocation().add(0.5, 1, 0.5);
        final PlayerCache cache = PlayerCache.from(player);

        // Support teleporting when the player is dead and respawning
        if (cache.hasPlayerTag("Respawning")) {
            PlayerRespawnEvent event = cache.getPlayerTag("Respawning");

            event.setRespawnLocation(topOfTheBlock);

        } else {
            Valid.checkBoolean(!player.isDead(), "Cannot teleport dead player " + player.getName());
            CompMetadata.setTempMetadata(player, TAG_TELEPORTING);

            final boolean success = player.teleport(topOfTheBlock, PlayerTeleportEvent.TeleportCause.PLUGIN);
            Valid.checkBoolean(success, "Failed to teleport " + player.getName() + " to both primary and fallback location, they may get stuck in the arena!");

            CompMetadata.removeTempMetadata(player, TAG_TELEPORTING);
        }
    }

    protected final void forEachPlayerInAllModes(final Consumer<Player> consumer) {
        this.forEachPlayer(consumer, null);
    }

    protected final void forEachPlayer(final Consumer<Player> consumer, final GameJoinMode mode) {
        for (final PlayerCache player : this.getPlayers(mode))
            consumer.accept(player.toPlayer());
    }

    protected final void forEachInAllModes(final Consumer<PlayerCache> consumer) {
        this.forEach(consumer, null);
    }

    protected final void forEach(final Consumer<PlayerCache> consumer, final GameJoinMode mode) {
        for (final PlayerCache player : this.getPlayers(mode))
            consumer.accept(player);
    }

    public final boolean isJoined(Player player) {
        for (final PlayerCache otherCache : this.players)
            if (otherCache.getUniqueId().equals(player.getUniqueId()))
                return true;

        return false;
    }

    public final boolean isJoined(PlayerCache cache) {
        return this.players.contains(cache);
    }

    public final List<Player> getBukkitPlayersInAllModes() {
        return this.getBukkitPlayers(null);
    }

    public final List<Player> getBukkitPlayers(final GameJoinMode mode) {
        return Common.convert(this.getPlayers(mode), PlayerCache::toPlayer);
    }

    public final List<PlayerCache> getPlayersInAllModes() {
        return Collections.unmodifiableList(this.players.getSource());
    }

    public final List<PlayerCache> getPlayers(@Nullable final GameJoinMode mode) {
        final List<PlayerCache> foundPlayers = new ArrayList<>();

        for (final PlayerCache otherCache : this.players)
            if (!otherCache.isLeaving() && (mode == null || (otherCache.hasGame() && otherCache.getCurrentGameMode() == mode)))
                foundPlayers.add(otherCache);

        return Collections.unmodifiableList(foundPlayers);
    }

    public final PlayerCache findPlayer(final Player player) {
        this.checkIntegrity();

        for (final PlayerCache otherCache : this.players)
            if (otherCache.hasGame() && otherCache.getCurrentGame().equals(this) && otherCache.getUniqueId().equals(player.getUniqueId()))
                return otherCache;

        return null;
    }

    protected final void checkBoolean(boolean value, Player player, String errorMessage) {
        if (!value) {
            Messenger.error(player, errorMessage);

            this.cancelEvent();
        }
    }

    protected final void cancelEvent() {
        throw new EventHandledException(true);
    }

    protected final void returnHandled() {
        throw new EventHandledException(false);
    }

    private void checkIntegrity() {

        if (this.state == GameState.STOPPED)
            Valid.checkBoolean(this.players.isEmpty(), "Found players in stopped " + this.getName() + " game: " + this.players);

        int playing = 0, editing = 0, spectating = 0;

        for (final PlayerCache cache : this.players) {
            final Player player = cache.toPlayer();
            final GameJoinMode mode = cache.getCurrentGameMode();

            Valid.checkBoolean(player != null && player.isOnline(), "Found a disconnected player " + player + " in game " + this.getName());

            if (mode == GameJoinMode.PLAYING)
                playing++;

            else if (mode == GameJoinMode.EDITING)
                editing++;

            else if (mode == GameJoinMode.SPECTATING)
                spectating++;
        }

        if (editing > 0) {
            Valid.checkBoolean(this.state == GameState.EDITED, "Game " + this.getName() + " must be in EDIT mode not " + this.state + " while there are " + editing + " editing players!");
            Valid.checkBoolean(playing == 0 && spectating == 0, "Found " + playing + " and " + spectating + " players in edited game " + this.getName());
        }
    }

    /* ------------------------------------------------------------------------------- */
    public static Game createGame(@NonNull final String name, @NonNull final GameType type) {
        return loadedFiles.loadOrCreateItem(name, () -> type.instantiate(name));
    }

    public static void loadGames() {
        loadedFiles.loadItems();
    }

    public static void removeGame(final String gameName) {
        loadedFiles.removeItemByName(gameName);
    }

    public static boolean isGameLoaded(final String name) {
        return loadedFiles.isItemLoaded(name);
    }

    public static Game findByName(@NonNull final String name) {
        return loadedFiles.findItem(name);
    }

    public static List<Game> findByType(final GameType type) {
        final List<Game> items = new ArrayList<>();

        for (final Game item : getGames())
            if (item.getType() == type)
                items.add(item);

        return items;
    }

    public static Game findByLocation(final Location location) {
        for (final Game game : getGames())
            if (game.getRegion().isWhole() && game.getRegion().isWithin(location))
                return game;

        return null;
    }

    public static Game findByPlayer(final Player player) {
        return PlayerCache.from(player).getCurrentGame();
    }

    public static List<? extends Game> getGames() {
        return loadedFiles.getItems();
    }

    public static Set<String> getGameNames() {
        return loadedFiles.getItemNames();
    }
}
