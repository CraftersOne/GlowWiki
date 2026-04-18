package com.ehhthan.glowwiki.api.event.action;

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
                case PAGE_PROTECTIONS -> new PageProtectionsAction(section);
            };
        }
        throw new IllegalArgumentException("Section is not defined.");
    }

    enum ActionType {
        EDIT_CONTENT,
        DELETE_PAGE,
        PAGE_PROTECTIONS;

        private static ActionType get(String id) {
            id = id.toUpperCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
            return valueOf(id);
        }
    }

}
