package com.ehhthan.glowwiki.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import com.ehhthan.glowwiki.GlowWiki;
import com.ehhthan.glowwiki.api.audit.GlowAuditor;
import com.ehhthan.glowwiki.api.event.WikiEvent;
import com.ehhthan.glowwiki.api.upload.GlowUploader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("glowwiki|gw|wiki")
@CommandPermission("glowwiki.help")
@Description("Main glow wiki command.")
public class GlowWikiCommand extends BaseCommand {
    private final GlowWiki plugin;

    public GlowWikiCommand(GlowWiki plugin) {
        this.plugin = plugin;
    }

    @Subcommand("upload")
    @CommandPermission("glowwiki.upload")
    @Description("Upload a specific thing to the wiki.")
    @CommandCompletion("@upload-types")
    @Syntax("<upload-type>")
    public void onUploadCommand(CommandSender sender, GlowUploader.Type uploadType) {
        GlowUploader uploader = plugin.getUploader();
        if (sender instanceof Player player)
            uploader.upload(uploadType, player);
        else {
            throw new InvalidCommandArgument("This command can only be run by a player.");
        }
    }

    @Subcommand("audit")
    @CommandPermission("glowwiki.audit")
    @Description("Perform an audit.")
    @CommandCompletion("players @events 10")
    @Syntax("<players> <event-name> <delay>")
    public void onAuditCommand(CommandSender sender, @Values("players") String function, WikiEvent event, long delay) {
        GlowAuditor auditor = plugin.getAuditor();

        if (function.equalsIgnoreCase("players")) {
            auditor.runPlayerAudit(event, sender, delay);
        }

        sender.sendMessage("Performing audit...");
    }

    @Subcommand("reload")
    @CommandPermission("glowwiki.reload")
    @Description("Perform a reload.")
    public void onReloadCommand(CommandSender sender) {
        plugin.reload();

        TextComponent.Builder message = Component.text()
            .append(Component.text("Reloading GlowWiki.").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN))
            .appendNewline()
            .append(Component.text(String.format("Reloaded %d %s", plugin.getEvents().values().size(), "Events")).color(NamedTextColor.BLUE));

        sender.sendMessage(message);
    }
}
