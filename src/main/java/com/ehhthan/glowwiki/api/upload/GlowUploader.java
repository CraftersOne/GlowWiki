package com.ehhthan.glowwiki.api.upload;

import co.aikar.commands.InvalidCommandArgument;
import com.ehhthan.glowwiki.GlowWiki;
import com.ehhthan.glowwiki.api.event.action.EventAction;
import com.ehhthan.glowwiki.api.info.GlowInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.IOException;
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
                                pages.add(MiniMessage.miniMessage().serialize(page));
                            }

                            JsonArray pageArray = new Gson().toJsonTree(pages).getAsJsonArray();

                            String encodedPages = Base64.getEncoder().encodeToString(pageArray.toString().getBytes());

                            String wikiTitle = String.format("Book:%s:%s", author, title);


                            try {
                                plugin.getClient().request(List.of(
                                    EventAction.QueryPair.of("action", "edit"),
                                    EventAction.QueryPair.of("title", wikiTitle),
                                    EventAction.QueryPair.of("section", "0"),
                                    EventAction.QueryPair.of("text", String.format("{{Book|author=%s|title=%s|pages=%s}}", author, title, encodedPages)),
                                    EventAction.QueryPair.of("summary", "Bot book upload."),
                                    EventAction.QueryPair.of("createonly", "true")
                                ));

                                plugin.getClient().request(List.of(
                                    EventAction.QueryPair.of("action", "protect"),
                                    EventAction.QueryPair.of("title", wikiTitle),
                                    EventAction.QueryPair.of("protections", "edit=sysop"),
                                    EventAction.QueryPair.of("reason", "Books should not be edited.")
                                ));

                                Component component = MiniMessage.miniMessage().deserialize("You can embed your book anywhere on the wiki by clicking the text below to copy it." +
                                    "<newline><green><click:copy_to_clipboard:'{{:<title>}}'><hover:show_text:'Click to copy.'>{{:<title>}}</hover></click>", Placeholder.parsed("title", wikiTitle));
                                player.sendMessage("Your book has been created!");
                                player.sendMessage(component);
                            } catch (IOException e) {
                                throw new InvalidCommandArgument("Could not upload book.");
                            }
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
