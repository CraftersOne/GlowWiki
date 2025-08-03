package com.ehhthan.glowwiki.api.info;

import org.bukkit.OfflinePlayer;

import java.util.Locale;

public enum PlayerInfo implements GlowInfo {
    USERNAME {
        @Override
        public String build(OfflinePlayer player) {
            return player.getName();
        }
    },
    UUID {
        @Override
        public String build(OfflinePlayer player) {
            return player.getUniqueId().toString();
        }
    };

    public abstract String build(OfflinePlayer player);

    public static PlayerInfo get(String id) {
        id = id.toUpperCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
        return valueOf(id);
    }
}
