package com.ehhthan.glowwiki.api.event.action;

import com.ehhthan.glowwiki.api.info.GlowInfo;
import com.ehhthan.glowwiki.api.wiki.GlowClient;
import com.ehhthan.glowwiki.api.wiki.ParamPair;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PageProtectionsAction extends EventAction {
    private final String title, protections, reason;

    public PageProtectionsAction(ConfigurationSection section) {
        this(section.getString("title"), section.getString("protections"), section.getString("reason"));
    }

    public PageProtectionsAction(String title, String protections, String reason) {
        this.title = title;
        this.protections = protections;
        this.reason = reason;
    }

    @Override
    public boolean run(GlowClient client, OfflinePlayer player) {
        ParamPair.Parser parser = new ParamPair.Parser(player);

        List<ParamPair> query = new LinkedList<>(List.of(
            ParamPair.of("action", "protect")
        ));

        List<ParamPair> body = new LinkedList<>(List.of(
            parser.of("title", GlowInfo.parse(title, player)),
            ParamPair.of("protections", protections),
            ParamPair.of("reason", reason)
        ));

        try {
            client.botRequest(query, body);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}