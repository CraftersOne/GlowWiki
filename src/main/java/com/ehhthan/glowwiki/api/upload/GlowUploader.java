package com.ehhthan.glowwiki.api.upload;

import co.aikar.commands.InvalidCommandArgument;
import com.ehhthan.glowwiki.GlowWiki;
import com.ehhthan.glowwiki.api.event.action.EditContentAction;
import com.ehhthan.glowwiki.api.event.action.PageProtectionsAction;
import com.ehhthan.glowwiki.api.util.ComponentUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

public class GlowUploader {
    private final GlowWiki plugin;

    public GlowUploader(GlowWiki plugin) {
        this.plugin = plugin;
    }

    public void upload(Type type, Player player) {
        switch (type) {
            case BOOK -> {
                EntityEquipment equipment = player.getEquipment();
                if (equipment != null) {
                    ItemStack mainHand = equipment.getItemInMainHand();
                    if (mainHand.getType() == Material.WRITTEN_BOOK) {
                        if (mainHand.getItemMeta() instanceof BookMeta bookMeta) {
                            String author = (bookMeta.author() != null)
                                ? PlainTextComponentSerializer.plainText().serialize(bookMeta.author())
                                : "No Author";
                            String title = (bookMeta.author() != null)
                                ? PlainTextComponentSerializer.plainText().serialize(bookMeta.title())
                                : "Untitled";

                            List<String> pages = new LinkedList<>();

                            for (Component page :  bookMeta.pages()) {
                                pages.add(MiniMessage.miniMessage().serialize(ComponentUtil.convertLegacy(page)));
                            }

                            JsonArray pageArray = new Gson().toJsonTree(pages).getAsJsonArray();
                            Base64.Encoder encoder = Base64.getUrlEncoder();
                            String encodedPages = encoder.encodeToString(pageArray.toString().getBytes(StandardCharsets.UTF_8));
                            String encodedTitle = encoder.encodeToString(title.getBytes(StandardCharsets.UTF_8));
                            String wikiTitle = String.format("Book:%s:%s", author, encodedTitle);

                            EditContentAction editAction = new EditContentAction(wikiTitle, 0, String.format("{{Book|author=%s|title=%s|pages=%s}}",
                                author, encodedTitle, encodedPages), true);
                            PageProtectionsAction protectionAction = new PageProtectionsAction(wikiTitle, "edit=sysop", "Books should not be edited.");

                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                editAction.run(plugin.getClient(), player);
                                protectionAction.run(plugin.getClient(), player);
                            });

                            Component component = plugin.getMessages().parse("book-created", Placeholder.parsed("title", wikiTitle), Placeholder.parsed("link", plugin.getClient().link(wikiTitle)));

                            player.sendMessage(component);
                        }

                    } else {
                        throw new InvalidCommandArgument("You must be holding a written book.");
                    }

                }
            }
            default -> throw new InvalidCommandArgument("Upload type does not exist.");

        }
    }

    public enum Type {
        BOOK;
    }

}
