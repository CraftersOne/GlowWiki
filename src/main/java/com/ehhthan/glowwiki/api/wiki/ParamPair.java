package com.ehhthan.glowwiki.api.wiki;

import com.ehhthan.glowwiki.api.info.GlowInfo;
import org.bukkit.OfflinePlayer;

public class ParamPair {
    private final String key, value;

    ParamPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static ParamPair of(String key, String value) {
        return new ParamPair(key, value);
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    public static class Parser {
        private final OfflinePlayer player;

        public Parser(OfflinePlayer player) {
            this.player = player;
        }

        public ParamPair of(String key, String value) {
            return new ParamPair(key, GlowInfo.parse(value, player));
        }
    }
}
