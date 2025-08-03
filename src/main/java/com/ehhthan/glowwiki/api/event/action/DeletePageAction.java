package com.ehhthan.glowwiki.api.event.action;

import com.ehhthan.glowwiki.api.info.GlowInfo;
import com.ehhthan.glowwiki.api.wiki.GlowClient;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DeletePageAction extends EventAction {
    private final String title;

    public DeletePageAction(ConfigurationSection section) {
        this.title = section.getString("title");
    }

    @Override
    public boolean run(GlowClient client, OfflinePlayer player) {
        QueryPairParser parser = new QueryPairParser(player);

        List<QueryPair> pairs = new LinkedList<>(List.of(
            QueryPair.of("action", "delete"),
            QueryPair.of("summary", "Bot requested."),
            parser.of("title", GlowInfo.parse(title, player))
        ));

        try {
            client.request(pairs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
