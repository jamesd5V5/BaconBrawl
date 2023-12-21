package org.mammothplugins.baconBrawl.conversations;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mammothplugins.baconBrawl.PlayerCache;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.conversation.SimpleConversation;

public class MapCreatorConversation extends SimpleConversation {

    private Player player;

    public MapCreatorConversation(Player player) {
        this.player = player;
    }

    @Override
    protected Prompt getFirstPrompt() {
        return new Prompt() {
            @NotNull
            @Override
            public String getPromptText(@NotNull ConversationContext conversationContext) {
                return "Please type the name of the player.";
            }

            @Override
            public boolean blocksForInput(@NotNull ConversationContext conversationContext) {
                return false;
            }

            @Nullable
            @Override
            public Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {
                PlayerCache.from(player).getCurrentGame().setMapCreator(s);
                Common.tell(player, "Map creator set to " + s);
                return this;
            }
        };
    }

}
