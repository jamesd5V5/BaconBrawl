package org.mammothplugins.ssm.model;

public final class GameRegionManager {

    //private static final StrictMap<String, Clipboard> savedClipboards = new StrictMap<>();

    public static void saveRegion(final Game game) {
		/*final Region region = game.getRegion();
		
		if (!region.isWhole())
			return;
		
		final CuboidRegion cuboidRegion = new CuboidRegion(new BukkitWorld(region.getWorld()), toVector(region.getPrimary()), toVector(region.getSecondary()));
		final BlockArrayClipboard clipboard = new BlockArrayClipboard(cuboidRegion);
		
		System.out.println("TRYING TO SAVE GAME");
		
		try (EditSession editSession = createSession(cuboidRegion.getWorld())) {
		
			new ChunkedTask(1, 20) {
		
				// The copy operation we are using
				private Operation operation = new ForwardExtentCopy(editSession, cuboidRegion, clipboard, cuboidRegion.getMinimumPoint());
		
				// Resume the operation means the operation moves forward
				// until it can no longer be resumed
				@Override
				protected void onProcess(final int index) throws Throwable {
					System.out.println("RROCESSING");
		
					this.operation = this.operation.resume(new RunContext());
				}
		
				// Return true if the operation can be resumed, ie not null
				@Override
				protected boolean canContinue(final int index) {
					return this.operation != null;
				}
		
				@Override
				protected void onFinish(boolean gracefully) {
					System.out.println("REGION SAVED");
		
					if (gracefully) {
						if (savedClipboards.containsKey(game.getName()))
							savedClipboards.remove(game.getName());
		
						savedClipboards.put(game.getName(), clipboard);
					} else
						System.out.println("IGNORE SAVE DUE TO ERROR");
				}
		
			}.startChain();
		}*/
    }

    /**
     * Restore game region if it has been saved
     */
    public static void restoreRegion(final Game game) {
		/*final Clipboard clipboard = savedClipboards.removeWeak(game.getName());
		final Region region = game.getRegion();
		
		if (clipboard == null) {
			System.out.println("NOT RESTORE, NOTHING SAVED");
		
			return;
		}
		
		if (!game.isPlayed()) {
			System.out.println("GAME NOT PLAYED, SKIP RESTORE");
		
			return;
		}
		
		System.out.println("RESTORING REGION....");
		
		try (EditSession editSession = createSession(new BukkitWorld(region.getWorld()))) {
			final List<BlockVector3> vectors = Common.convert(region.getBlocks(), GameRegionManager::toVector);
		
			new ChunkedTask(30_000, 20) {
		
				// For each block in region find block stored in the clipboard,
				// if it exists, restore it back
				@Override
				protected void onProcess(final int index) throws Throwable {
					final BlockVector3 vector = vectors.get(index);
					final BaseBlock copy = clipboard.getFullBlock(vector);
		
					if (copy != null) {
						editSession.setBlock(vector, copy);
		
						editSession.close();
					}
				}
		
				// Flush the operation to make the blocks visible on finish
				@Override
				protected void onFinish(boolean gracefully) {
					System.out.println("REGION RESTORED.");
		
					editSession.close();
				}
		
				// Return if we can pull more blocks from our region or we are finished
				@Override
				protected boolean canContinue(final int index) {
					return index < vectors.size();
				}
		
				// Also show percentage how many blocks we have restored to finish
				@Override
				protected String getProcessMessage(final long initialTime, final int processed) {
					final long progress = Math.round(((double) this.getCurrentIndex() / (double) vectors.size()) * 100);
		
					return "[" + progress + "%] " + super.getProcessMessage(initialTime, processed);
				}
		
			}.startChain();
		}*/
    }

    // Create a new edit session
	/*private static EditSession createSession(final World world) {
		EditSession session = WorldEdit.getInstance().newEditSession(world);
		//session.setReorderMode(EditSession.ReorderMode.FAST);
	
		return session;
	}
	
	//
	// Create a WorldEdit vector from the given block
	//
	private static BlockVector3 toVector(final Block block) {
		return toVector(block.getLocation());
	}
	
	//
	// Create a WorldEdit vector from the given location
	//
	private static BlockVector3 toVector(final Location location) {
		return BlockVector3.at(location.getX(), location.getY(), location.getZ());
	}*/
}