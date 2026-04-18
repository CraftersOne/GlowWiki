package com.ehhthan.glowwiki;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.ehhthan.glowwiki.api.audit.GlowAuditor;
import com.ehhthan.glowwiki.api.event.WikiEvent;
import com.ehhthan.glowwiki.api.event.WikiEventManager;
import com.ehhthan.glowwiki.api.upload.GlowUploader;
import com.ehhthan.glowwiki.api.wiki.GlowClient;
import com.ehhthan.glowwiki.command.GlowWikiCommand;
import com.ehhthan.glowwiki.file.DirectoryCopyFileVisitor;
import com.ehhthan.glowwiki.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

public final class GlowWiki extends JavaPlugin {
    private static GlowWiki INSTANCE;
    private WikiEventManager events;
    private MessageManager messages;
    private GlowAuditor auditor;
    private GlowUploader uploader;
    private PlayerListener playerListener;
    private GlowClient glowClient;

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Default file generation.
        saveDefaultConfig();
        try {
            DirectoryCopyFileVisitor.INSTANCE.copy("default", getDataFolder().toPath());
        } catch (URISyntaxException | IOException e) {
            getLogger().log(Level.SEVERE, "Could not generate default files.");
            e.printStackTrace();
        }

        ConfigurationSection section = getConfig().getConfigurationSection("wiki");
        if (section != null) {
            this.glowClient = new GlowClient(section);
        }

        this.events = new WikiEventManager(this);
        this.messages = new MessageManager(this);
        this.auditor = new GlowAuditor(this);
        this.uploader = new GlowUploader(this);
        this.playerListener = new PlayerListener(this);

        Bukkit.getPluginManager().registerEvents(playerListener, this);
        registerCommands();
    }

    public void reload() {
        events.reload(this);
        messages.reload(this);
        playerListener.reload(events);
        glowClient.refresh();
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.getCommandCompletions().registerCompletion("events", c -> events.values().stream().map(WikiEvent::getId).toList());
        commandManager.getCommandCompletions().registerCompletion("upload-types", c -> Arrays.stream(GlowUploader.Type.values()).map((type) -> type.name().toLowerCase(Locale.ROOT)).toList());

        commandManager.getCommandContexts().registerContext(WikiEvent.class, event -> {
            String id = event.popFirstArg();

            if (events.hasEvent(id)) {
                return events.getEvent(id);
            }

            throw new InvalidCommandArgument("Event does not exist.");
        });

        commandManager.getCommandContexts().registerContext(GlowUploader.Type.class, event -> {
            String id = event.popFirstArg().toUpperCase();

            try {
                return GlowUploader.Type.valueOf(id);
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new InvalidCommandArgument("Upload type does not exist.");
            }
        });

        commandManager.registerCommand(new GlowWikiCommand(this));
    }


    public static GlowWiki getInstance() {
        return INSTANCE;
    }

    public WikiEventManager getEvents() {
        return events;
    }

    public MessageManager getMessages() {
        return messages;
    }

    public GlowAuditor getAuditor() {
        return auditor;
    }

    public GlowUploader getUploader() {
        return uploader;
    }

    public GlowClient getClient() {
        return glowClient;
    }
}
