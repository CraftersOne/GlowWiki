package com.ehhthan.glowwiki.api.audit;

import com.ehhthan.glowwiki.GlowWiki;
import com.ehhthan.glowwiki.api.event.WikiEvent;
import com.ehhthan.glowwiki.api.event.action.EventAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Iterator;

public class GlowAuditor {
    private final GlowWiki plugin;

    public GlowAuditor(GlowWiki plugin) {
        this.plugin = plugin;
    }

    public void runPlayerAudit(WikiEvent event, CommandSender sender, long delay) {
        if (event != null) {
            Iterator<OfflinePlayer> players = Arrays.stream(Bukkit.getOfflinePlayers()).iterator();
            Bukkit.getScheduler().runTaskTimer(plugin, (task) -> {
                if (players.hasNext()) {
                    OfflinePlayer player = players.next();
                    sender.sendMessage("Auditing Player: " + player.getName());
                    run(player, event);
                } else {
                    sender.sendMessage("Auditing Complete. ");
                    task.cancel();
                }

            }, 1L, delay);
        }
    }

    private void run(OfflinePlayer player, WikiEvent event) {
        for (EventAction action : event.getActions()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                 action.run(plugin.getClient(), player);
            });
        }
    }
}
