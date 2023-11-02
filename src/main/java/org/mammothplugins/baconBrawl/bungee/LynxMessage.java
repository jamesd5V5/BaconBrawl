package org.mammothplugins.baconBrawl.bungee;

import lombok.Getter;
import org.mineacademy.fo.bungee.BungeeMessageType;
import org.mineacademy.fo.collection.SerializedMap;

public enum LynxMessage implements BungeeMessageType {

    CHAT_MESSAGE(
            String.class, // senders name
            String.class // message
    ),

    DATA_SYNC(
            SerializedMap.class // data as map<server name, data as json>
    );

    //PLAY_SOUND(UUID.class, String.class, Float.class, Float.class);

    @Getter
    private final Class<?>[] content;

    LynxMessage(Class<?>... content) {
        this.content = content;
    }
}
