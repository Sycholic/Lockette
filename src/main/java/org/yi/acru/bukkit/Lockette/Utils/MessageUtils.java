/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yi.acru.bukkit.Lockette.Utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.yi.acru.bukkit.Lockette.Lockette;

/**
 *
 * @author Samuel
 */
public class MessageUtils {
    private final Lockette plugin;

    public MessageUtils(Lockette plugin) {
        this.plugin = plugin;
    }

    public void localizedMessage(Player player, String broadcast, String key) {
        localizedMessage(player, broadcast, key, null, null);
    }

    public void localizedMessage(Player player, String broadcast, String key, String sub) {
        localizedMessage(player, broadcast, key, sub, null);
    }

    public void localizedMessage(Player player, String broadcast, String key, String sub, String num) {
        String color = "";

        // Filter and color based on message type.
        if (key.startsWith("msg-user-")) {
            if (broadcast == null) {
                if (!plugin.msgUser) {
                    return;
                }
            }
            color = ChatColor.YELLOW.toString();
        } else if (key.startsWith("msg-owner-")) {
            if (broadcast == null) {
                if (!plugin.msgOwner) {
                    return;
                }
            }
            color = ChatColor.GOLD.toString();
        } else if (key.startsWith("msg-admin-")) {
            if (broadcast == null) {
                if (!plugin.msgAdmin) {
                    return;
                }
            }
            color = ChatColor.RED.toString();
        } else if (key.startsWith("msg-error-")) {
            if (broadcast == null) {
                if (!plugin.msgError) {
                    return;
                }
            }
            color = ChatColor.RED.toString();
        } else if (key.startsWith("msg-help-")) {
            if (broadcast == null) {
                if (!plugin.msgHelp) {
                    return;
                }
            }
            color = ChatColor.GOLD.toString();
        }

        // Fetch the requested message string.
        String message = plugin.strings.getString(key);
        if ((message == null) || message.isEmpty()) {
            return;
        }

        // Do place holder substitution.
        message = message.replaceAll("&([0-9A-Fa-f])", "\u00A7$1");
        if (sub != null) {
            message = message.replaceAll("\\*\\*\\*", sub + color);
        }
        if (num != null) {
            message = message.replaceAll("###", num);
        }
        if (player != null) {
            message = message.replaceAll("@@@", player.getName());
        }

        // Send out the formatted message.
        if (broadcast != null) {
            plugin.selectiveBroadcast(broadcast, color + "[Lockette] " + message);
        } else if (player != null) {
            player.sendMessage(color + "[Lockette] " + message);
        }
    }

}
