/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yi.acru.bukkit.Lockette;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import static org.yi.acru.bukkit.Lockette.Lockette.plugin;
import static org.yi.acru.bukkit.PluginCore.getSignAttachedBlock;

/**
 *
 * @author Samuel
 */
public class LocketteAPI {

    private final Lockette plugin;

    public LocketteAPI(Lockette plugin) {
        this.plugin = plugin;
    }

    //********************************************************************************************************************
    // Start of public section
    /**
     * Check if a chest/block is protected
     *
     * @param block
     * @return boolean based on result
     */
    public boolean isProtected(Block block) {
        if (!plugin.isEnabled()) {
            return (false);
        }

        int type = block.getTypeId();

        if (type == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign) block.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

            if (text.equals("[private]") || text.equalsIgnoreCase(plugin.altPrivate)) {
                return (true);
            } else if (text.equals("[more users]") || text.equalsIgnoreCase(plugin.altMoreUsers)) {
                Block checkBlock = getSignAttachedBlock(block);

                if (checkBlock != null) {
                    if (plugin.findBlockOwner(checkBlock) != null) {
                        return (true);
                    }
                }
            }
        } else if (plugin.findBlockOwner(block) != null) {
            return (true);
        }

        return (false);
    }

    public String getProtectedOwner(Block block) {
        return Bukkit.getOfflinePlayer(getProtectedOwnerUUID(block)).getName();
    }

    public UUID getProtectedOwnerUUID(Block block) {
        if (!plugin.isEnabled()) {
            return (null);
        }

        int type = block.getTypeId();

        if (type == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign) block.getState();
            String text = ChatColor.stripColor(sign.getLine(0)).toLowerCase();

            if (text.equals("[private]") || text.equalsIgnoreCase(plugin.altPrivate)) {
                return plugin.signUtil.getUUIDFromMeta(sign, 1);
            } else if (text.equals("[more users]") || text.equalsIgnoreCase(plugin.altMoreUsers)) {
                Block checkBlock = getSignAttachedBlock(block);

                if (checkBlock != null) {
                    Block signBlock = plugin.findBlockOwner(checkBlock);

                    if (signBlock != null) {
                        sign = (Sign) signBlock.getState();
                        return plugin.signUtil.getUUIDFromMeta(sign, 1);
                    }
                }
            }
        } else {
            Block signBlock = plugin.findBlockOwner(block);
            if (signBlock != null) {
                Sign sign = (Sign) signBlock.getState();
                return plugin.signUtil.getUUIDFromMeta(sign, 1);
            }
        }

        return null;
    }

    public boolean isEveryone(Block block) {
        if (!plugin.isEnabled()) {
            return (true);
        }

        Block signBlock = plugin.findBlockOwner(block);

        if (signBlock == null) {
            return (true);
        }

        // Check main three users.
        Sign sign = (Sign) signBlock.getState();
        String line;
        int y;

        for (y = 1; y <= 3; ++y) {
            if (!sign.getLine(y).isEmpty()) {
                line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

                if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(plugin.altEveryone)) {
                    return (true);
                }
            }
        }

        // Check for more users.
        List<Block> list = plugin.findBlockUsers(block, signBlock);
        int x, count = list.size();

        for (x = 0; x < count; ++x) {
            sign = (Sign) list.get(x).getState();

            for (y = 1; y <= 3; ++y) {
                if (!sign.getLine(y).isEmpty()) {
                    line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

                    if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(plugin.altEveryone)) {
                        return (true);
                    }
                }
            }
        }

        // Everyone doesn't have permission.
        return (false);
    }
    
        public boolean isOwner(Block block, String name) {
        return isOwner(block, Bukkit.getOfflinePlayer(name));
    }

    public boolean isUser(Block block, String name, boolean withGroups) {
        return isUser(block, Bukkit.getOfflinePlayer(name), withGroups);
    }

    public boolean isOwner(Block block, OfflinePlayer player) {
        if (!plugin.isEnabled()) {
            return true;
        }

        Block checkBlock = plugin.findBlockOwner(block);
        if (checkBlock == null) {
            return true;
        }

        Sign sign = (Sign) checkBlock.getState();

        // Check owner only.
        return plugin.matchUserUUID(sign, 1, player, true);
    }

    public boolean isOwner(Sign sign, OfflinePlayer player) {
        // Check owner only.
        return plugin.matchUserUUID(sign, 1, player, true);
    }

    public boolean isUser(Block block, OfflinePlayer player, boolean withGroups) {
        if (!plugin.isEnabled()) {
            return true;
        }

        Block signBlock = plugin.findBlockOwner(block);

        if (signBlock == null) {
            return true;
        }

        // Check main three users.
        Sign sign = (Sign) signBlock.getState();

        for (int y = 1; y <= 3; ++y) {
            String line = sign.getLine(y);
            if (plugin.matchUserUUID(sign, y, player, true)) {// Check if the name is there verbatum.
                return true;
            }

            // Check if name is in a group listed on the sign.
            if (withGroups) {
                if (plugin.inGroup(block.getWorld(), player.getName(), line)) {
                    return true;
                }
            }
        }

        // Check for more users.
        List<Block> list = plugin.findBlockUsers(block, signBlock);
        for (Block blk : list) {
            sign = (Sign) blk.getState();

            for (int y = 1; y <= 3; y++) {
                String line = sign.getLine(y);
                if (plugin.matchUserUUID(sign, y, player, true)) {// Check if the name is there verbatum.
                    return true;
                }

                // Check if name is in a group listed on the sign.
                if (withGroups) {
                    if (plugin.inGroup(block.getWorld(), player.getName(), line)) {
                        return true;
                    }
                }
            }
        }

        // User doesn't have permission.
        return false;
    }
}
