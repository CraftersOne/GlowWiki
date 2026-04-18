package com.ehhthan.glowwiki;

import com.ehhthan.glowwiki.api.util.FormatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final Map<String, String> messages = new HashMap<>();

    public MessageManager(@NotNull GlowWiki plugin) {
        reload(plugin);
    }

    public void reload(@NotNull GlowWiki plugin) {
        messages.clear();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        for (String key : config.getKeys(false)) {
            key = FormatUtil.id(key);
            if (config.isList(key)) {
                messages.put(key, String.join("<br>", config.getStringList(key)));
            } else if (config.isString(key)) {
                messages.put(key, config.getString(key));
            }
        }
    }

    public boolean has(@NotNull String key) {
        return messages.containsKey(FormatUtil.id(key));
    }

    @Nullable
    public String get(@NotNull String key) {
        return messages.get(FormatUtil.id(key));
    }

    public Component parse(@NotNull String key, TagResolver... resolvers) {
        return MiniMessage.miniMessage().deserialize(messages.getOrDefault(FormatUtil.id(key), "No message."), resolvers);
    }
}
