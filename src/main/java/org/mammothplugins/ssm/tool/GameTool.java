package org.mammothplugins.ssm.tool;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.mammothplugins.ssm.PlayerCache;
import org.mammothplugins.ssm.model.Game;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.visual.VisualTool;

import javax.annotation.Nullable;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GameTool extends VisualTool {

    @Override
    public void handleBlockClick(Player player, ClickType click, Block block) {
        final Game game = PlayerCache.from(player).getCurrentGame();

        if (game == null) {
            Messenger.error(player, "You must be editing a game to use this tool!");

            return;
        }

        if (!game.isEdited()) {
            Messenger.error(player, "You can only use this tool in an edited game!");

            return;
        }

        // Handle parent
        super.handleBlockClick(player, click, block);

        // Post to us
        this.onSuccessfulClick(player, game, block, click);

        // Save data
        game.save();
    }

    @Override
    protected void handleAirClick(Player player, ClickType click) {
        Game game = this.getCurrentGame(player);

        if (game != null && game.isEdited())
            this.onSuccessfulAirClick(player, game, click);
    }

    protected void onSuccessfulAirClick(Player player, Game game, ClickType click) {
    }

    protected void onSuccessfulClick(Player player, Game game, Block block, ClickType click) {
    }

    @Override
    protected List<Location> getVisualizedPoints(Player player) {
        final List<Location> points = super.getVisualizedPoints(player);
        final Game game = this.getCurrentGame(player);

        if (game != null) {
            final Location point = this.getGamePoint(player, game);

            if (point != null)
                points.add(point);

            final List<Location> additionalPoints = this.getGamePoints(player, game);

            if (additionalPoints != null)
                points.addAll(additionalPoints);
        }

        return points;
    }

    @Nullable
    protected Location getGamePoint(Player player, Game game) {
        return null;
    }

    @Nullable
    protected List<Location> getGamePoints(Player player, Game game) {
        return null;
    }

    protected Game getCurrentGame(Player player) {
        return PlayerCache.from(player).getCurrentGame();
    }
}
