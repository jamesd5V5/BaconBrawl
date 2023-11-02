package org.mammothplugins.baconBrawl.model;

import lombok.SneakyThrows;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mammothplugins.baconBrawl.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictSet;
import org.mineacademy.fo.model.ChunkedTask;
import org.mineacademy.fo.region.Region;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enable us to reset game world by
 * 1) Stopping the autosave when the game starts
 * 2) Restoring back chunks to their previous state on game end
 */
public final class GameWorldManager {

    /**
     * A list of worlds that are being processes
     */
    private static final StrictSet<World> processedWorlds = new StrictSet<>();

    /**
     * Disable autosave for the given world
     *
     * @param game
     */
    public static void disableAutoSave(Game game) {
        checkApplicable(game);

        final World world = game.getRegion().getWorld();
        world.setAutoSave(false);

        Common.log("Game " + game.getName() + " disabled world save for " + world.getName());
    }

    /**
     * Attempts to restore all chunks in the world before autosave was disabled
     *
     * @param game
     */
    @SneakyThrows
    public static void restoreWorld(Game game) {
        checkApplicable(game);

        final Region region = game.getRegion();
        final World world = region.getWorld();

        Valid.checkBoolean(world.getPlayers().isEmpty(), "Cannot restore game world " + world.getName() + " when found players in it: " + Common.convert(world.getPlayers(), Player::getName));
        processedWorlds.add(world);

        System.out.println("COUNTING CHUNKS FOR WORLD RESTORE...");

        final List<Block> blocks = region.getBlocks(); // 2M blocks

        System.out.println("FOUND BLOCKS: " + blocks.size());

        // Convert blocks into chunks
        new ChunkedTask(1_000_000) {

            final Set<Chunk> chunks = new HashSet<>();

            @Override
            protected void onProcess(int index) {
                final Block block = blocks.get(index);

                chunks.add(block.getChunk());
            }

            @Override
            protected boolean canContinue(int index) {
                return index < blocks.size();
            }

            @Override
            protected String getLabel() {
                return "blocks";
            }

            @Override
            protected void onFinish(boolean gracefully) {

                System.out.println("CONVERTED INTO " + chunks.size() + " CHUNKS");

                new ChunkedTask(50) {

                    final List<Chunk> chunksCopy = new ArrayList<>(chunks);

                    @Override
                    protected void onProcess(int index) {
                        final Chunk chunk = chunksCopy.get(index);

                        chunk.unload(false);
                        chunk.load();
                    }

                    @Override
                    protected boolean canContinue(int index) {
                        return index < chunksCopy.size();
                    }

                    @Override
                    protected String getLabel() {
                        return "chunks";
                    }

                    @Override
                    protected void onFinish(boolean gracefully) {
                        Common.log("Game " + game.getName() + " finished resetting world " + world.getName() + ".");

                        processedWorlds.remove(world);
                    }
                }.startChain();
            }
        }.startChain();
    }

    /**
     * Return if the given world is being processed right now
     *
     * @param world
     * @return
     */
    public static boolean isWorldBeingProcessed(World world) {
        return processedWorlds.contains(world);
    }

    /*
     * Check a few settings if we can proceed
     */
    private static void checkApplicable(Game game) {
        Valid.checkBoolean(!isWorldBeingProcessed(game.getRegion().getWorld()), "Game " + game.getName() + " world is already being processed!");

        if (!Settings.AutoMode.ENABLED) {
            Valid.checkNotNull(game.getReturnBackLocation(), "Cannot use world restore, reset location is empty!");
            Valid.checkBoolean(!game.getReturnBackLocation().getWorld().equals(game.getLobbyLocation().getWorld()), "Return back location must be in a different world for game: " + game.getName());
        }
    }
}
