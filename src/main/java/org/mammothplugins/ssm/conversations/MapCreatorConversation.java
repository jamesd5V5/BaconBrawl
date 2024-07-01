package org.mammothplugins.ssm.conversations;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.mammothplugins.ssm.PlayerCache;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.conversation.SimplePrompt;
import org.mineacademy.fo.remain.CompSound;

public class MapCreatorConversation extends SimplePrompt {
    public MapCreatorConversation() {
        super(false);
    }

    protected String getPrompt(ConversationContext conversationContext) {
        return "&aPlease type the name of the map creator. &7Type &oexit &r&7to leave.";
    }

    protected boolean isInputValid(ConversationContext context, String input) {
        return true;
    }

    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        Player player = this.getPlayer(context);
        PlayerCache cache = PlayerCache.from(player);
        cache.getCurrentGame().setMapCreator(input);
        CompSound.CAT_MEOW.play(player, 5.0F, 0.3F);
        Common.tell(player, "&7You have &asucessfully &7set the map creator's name &a" + input + "&7.");
        return null;
    }
}
