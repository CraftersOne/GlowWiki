package com.ehhthan.glowwiki.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ComponentUtil {

    public static Component convertLegacy(Component component) {
        String serialized = MiniMessage.miniMessage().serialize(component);
        return LegacyComponentSerializer.legacySection().deserialize(serialized);
    }
}
