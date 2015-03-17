/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yi.acru.bukkit.Lockette.Utils;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.yi.acru.bukkit.Lockette.Lockette;

/**
 *
 * @author Samuel
 */
public class SignUtil {

    private final Lockette plugin;

    public SignUtil(Lockette instance) {
        plugin = instance;
    }

    public void setLine(org.bukkit.block.Sign sign, int index, String typed) {
        // check whether we should continue with uuid support or not.
        OfflinePlayer player = null;
        if (!typed.isEmpty() && typed.indexOf("[") != 0) {
            String id = ChatColor.stripColor(typed.replaceAll("&([0-9A-Fa-f])", ""));
            player = Bukkit.getOfflinePlayer(id);
        }

        // if player is "null", then typed string will just be set.
        setLine(sign, index, typed, player);
    }

    public void setLine(org.bukkit.block.Sign sign, int index, String typed, OfflinePlayer player) {
        // set whatever typed on the sign anyway.
        String cline = typed.replaceAll("&([0-9A-Fa-f])", "\u00A7$1");
        sign.setLine(index, cline);
        sign.update(true);

        UUID[] uuids = null;
        if (!sign.hasMetadata(plugin.getMETA_KEY())) {
            uuids = new UUID[3];
            sign.setMetadata(plugin.getMETA_KEY(), new FixedMetadataValue(plugin, uuids));
        } else {
            List<MetadataValue> list = sign.getMetadata(plugin.getMETA_KEY());
            // should be only one MetadataValue	
            uuids = (UUID[]) list.get(0).value();
        }
        uuids[index - 1] = (player != null) ? player.getUniqueId() : null;
        if (plugin.getDEBUG()) {
            plugin.log.info("[Lockette] setting the line " + index + " to " + cline);
            plugin.log.info("[Lockette] corresponding player is " + player);
            plugin.log.info("[Lockette] uuid has been attached: " + uuids[index - 1]);
        }
    }
    
    public UUID getUUIDFromMeta(Sign sign, int index) {
        if (sign.hasMetadata(plugin.getMETA_KEY())) {
            List<MetadataValue> list = sign.getMetadata(plugin.getMETA_KEY());
            // should be only one MetadataValue	
            return ((UUID[]) list.get(0).value())[index - 1];
        }
        return null;
    }
    
    public void removeUUIDMetadata(Sign sign) {
        if (sign.hasMetadata(plugin.getMETA_KEY())) {
            sign.removeMetadata(plugin.getMETA_KEY(), plugin);
        }
    }
}
