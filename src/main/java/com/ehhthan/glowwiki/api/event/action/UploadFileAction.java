package com.ehhthan.glowwiki.api.event.action;

import com.ehhthan.glowwiki.GlowWiki;
import com.ehhthan.glowwiki.api.info.GlowInfo;
import com.ehhthan.glowwiki.api.wiki.WikiAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class UploadFileAction extends EventAction {
    private final String url, name, format;

    public UploadFileAction(ConfigurationSection section) {
        this.url = section.getString("url");
        this.name = section.getString("name");
        this.format = section.getString("format", "png");
    }


    @Override
    public void run(WikiAPI api, OfflinePlayer player) {
        try {
            URL url = new URL(GlowInfo.parse(this.url, player));

            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "GlowWiki/1.2 (+https://wiki.crafters.one; https://github.com/Ehhthan/GlowWiki; <ethan@ehhthan.com>)");
            conn.connect();

            InputStream urlStream = conn.getInputStream();
            BufferedImage image = ImageIO.read(urlStream);

            File file = new File(GlowWiki.getInstance().getDataFolder(), "cache/" +
                GlowInfo.parse(name + "." + format, player));

            if (!api.exists(List.of("File:" + file.getName()))[0]) {
                file.mkdirs();
                ImageIO.write(image, GlowInfo.parse(format, player), file);
                api.upload(file, file.getName(), "", "Uploaded file.");
                file.deleteOnExit();
            }
        } catch (IOException | LoginException e) {
            e.printStackTrace();
        }
    }
}
