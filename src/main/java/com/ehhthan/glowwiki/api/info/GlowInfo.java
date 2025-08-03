package com.ehhthan.glowwiki.api.info;

import org.bukkit.OfflinePlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface GlowInfo {
    Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");

    static String parse(String text, OfflinePlayer player) {
        String newText = text;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);

        while (matcher.find()) {
            newText = newText.replace(matcher.group(), PlayerInfo.get(matcher.group(1)).build(player));
        }

        return newText;
    }
}
