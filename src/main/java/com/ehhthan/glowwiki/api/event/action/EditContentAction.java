package com.ehhthan.glowwiki.api.event.action;


import com.ehhthan.glowwiki.GlowWiki;
import com.ehhthan.glowwiki.api.info.GlowInfo;
import com.ehhthan.glowwiki.api.wiki.GlowClient;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class EditContentAction extends EventAction {
    private final String title;
    private final int section;
    private final String content;
    private final boolean createOnly;

    public EditContentAction(ConfigurationSection section) {
        this.title = section.getString("title");
        this.section = section.getInt("section");

        String content = "";
        if (section.isString("file")) {
            File file = new File(GlowWiki.getInstance().getDataFolder(), "content/" + section.getString("file"));
            try {
                content = Files.readString(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (section.isString("content")) {
            content = section.getString("content");
        }

        this.content = content;
        this.createOnly = section.getBoolean("create-only", false);
    }

    @Override
    public boolean run(GlowClient client, OfflinePlayer player) {
        QueryPairParser parser = new QueryPairParser(player);

        List<QueryPair> pairs = new LinkedList<>(List.of(
            QueryPair.of("action", "edit"),
            parser.of("title", GlowInfo.parse(title, player)),
            parser.of("section", String.valueOf(section)),
            parser.of("text", content),
            QueryPair.of("summary", "Bot requested.")
        ));

        if (createOnly) {
            pairs.add(QueryPair.of("createonly", "true"));
        }

        try {
            client.request(pairs);
            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
