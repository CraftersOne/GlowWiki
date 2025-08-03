package com.ehhthan.glowwiki.api.event.action;

import com.ehhthan.glowwiki.api.info.GlowInfo;
import com.ehhthan.glowwiki.api.wiki.GlowClient;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

public abstract class EventAction {
    public EventAction() {
    }

    public abstract boolean run(GlowClient client, OfflinePlayer player);

    public static EventAction get(ConfigurationSection section) {
        if (section != null) {
            return switch (ActionType.get(section.getString("type"))) {
                case EDIT_CONTENT -> new EditContentAction(section);
                case DELETE_PAGE -> new DeletePageAction(section);
            };
        }
        throw new IllegalArgumentException("Section is not defined.");
    }

    enum ActionType {
        EDIT_CONTENT,
        DELETE_PAGE;

        private static ActionType get(String id) {
            id = id.toUpperCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
            return valueOf(id);
        }
    }

    public static class QueryPairParser {
        private final OfflinePlayer player;

        public QueryPairParser(OfflinePlayer player) {
            this.player = player;
        }

        public QueryPair of(String key, String value) {
            return new QueryPair(key, GlowInfo.parse(value, player));
        }
    }

    public static class QueryPair {
        private final String key, value;

        private QueryPair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static QueryPair of(String key, String value) {
            return new QueryPair(key, value);
        }

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }
    }
}
