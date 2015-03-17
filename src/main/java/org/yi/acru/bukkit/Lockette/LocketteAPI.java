/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yi.acru.bukkit.Lockette;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import static org.yi.acru.bukkit.PluginCore.getSignAttachedBlock;

/**
 *
 * @author Samuel
 */
public class LocketteAPI {
    
    private Lockette plugin;

    public LocketteAPI(Lockette plugin) {
        this.plugin = plugin;
    }
        //********************************************************************************************************************
    // Start of public section
    /**
     * Check if a chest/block is protected
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
    
    
    
}
