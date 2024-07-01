package org.mammothplugins.ssm.apis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mammothplugins.ssm.PlayerCache;
import org.mineacademy.fo.model.SimpleExpansion;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlaceHolderApi extends SimpleExpansion {

    @Getter
    private static final SimpleExpansion instance = new PlaceHolderApi();

//    @Override
//    public String onRequest(OfflinePlayer player, String params) {
//        if (player != null)
//            return onPlaceholderRequest(player.getPlayer(), params);
//        return null;
//    }
//
//    @Override
//    public String onPlaceholderRequest(Player player, String params) {
//        PlayerCache cache = PlayerCache.from(player);
//
//        //%ssm_Placeholder%
//        if (params.equalsIgnoreCase("gamesPlayed"))
//            return cache.getGamesPlayed() + "";
//        if (params.equalsIgnoreCase("gamesWon"))
//            return cache.getGamesWon() + "";
//        if (params.equalsIgnoreCase("kills"))
//            return cache.getKills() + "";
//
//        String ratio = "0";
//        if (cache.getGamesPlayed() != 0)
//            ratio = "" + (cache.getGamesWon() / cache.getGamesPlayed());
//        if (params.equalsIgnoreCase("ratio"))
//            return ratio;
//
//        String currentGame = "";
//        if (cache.getCurrentGame() != null)
//            currentGame = cache.getCurrentGameName() + "";
//        if (params.equalsIgnoreCase("currentGame"))
//            return currentGame;
//
//        String kit = "ElMuchachoPig";
//        if (cache.getCurrentKit() != null)
//            kit = cache.getCurrentKit().getName() + "";
//        if (params.equalsIgnoreCase("kit"))
//            return kit;
//
//        return null;
//    }


    @Override
    protected String onReplace(@NonNull CommandSender sender, String identifier) {
        final Player player = sender instanceof Player && ((Player) sender).isOnline() ? (Player) sender : null;

        //
        // Static variables,
        // implement your logic here
        //
        PlayerCache cache = PlayerCache.from(player);
        String gameRatio = "0";
        String killRatio = "0";
        if (cache.getGamesPlayed() != 0) {
            double num = (double) cache.getGamesWon() / cache.getGamesPlayed();
            String numb = String.format("%.2f", num);
            gameRatio = "" + numb;

            double other = (double) cache.getKills() / cache.getGamesPlayed();
            String numbb = String.format("%.2f", other);
            killRatio = "" + numbb;
        }
        String currentGame = "";
        if (cache.getCurrentGame() != null)
            currentGame = cache.getCurrentGameName() + "";
        String kit = "ElMuchachoPig";
        if (cache.getCurrentKit() != null)
            kit = cache.getCurrentKit().getName() + "";


        switch (identifier) {
            case "gamesPlayed":
                return cache.getGamesPlayed() + "";
            case "gamesWon":
                return cache.getGamesWon() + "";
            case "kills":
                return cache.getKills() + "";
            case "gameRatio":
                return gameRatio;
            case "killRatio":
                return killRatio;
            case "currentGame":
                return currentGame;
            case "kit":
                return kit;
        }

        //
        // Dynamic variables, with an example,
        // implement your logic here
        //
//        if (identifier.startsWith("player_has_gamemode_")) {
//
//            // Fix for Discord or console sender
//            if (player == null)
//                return "false";
//
//            final String gamemodeName = join(3);
//            final GameMode gamemode = ReflectionUtil.lookupEnumSilent(GameMode.class, gamemodeName.toUpperCase());
//
//            return gamemode == null ? "invalid" : player.getGameMode() == gamemode ? "true" : "false";
//        }

        return NO_REPLACE;
    }
}
