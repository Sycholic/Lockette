//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//
package org.yi.acru.bukkit.Lockette.Listeners;

//Imports.
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.world.StructureGrowEvent;

import org.yi.acru.bukkit.BlockUtil;
import org.yi.acru.bukkit.Lockette.Lockette;

public class LocketteWorldListener implements Listener {

    private final Lockette plugin;

    public LocketteWorldListener(Lockette instance) {
        plugin = instance;
    }


    // Start of event section
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (event.isCancelled()) {
            return;
        }

        List<BlockState> blockList = event.getBlocks();
        int x, count = blockList.size();
        Block block;
        // Check the block list for any protected blocks, and cancel the event if any are found.
        for (x = 0; x < count; ++x) {
            block = blockList.get(x).getBlock();

            if (plugin.isProtected(block)) {
                event.setCancelled(true);
                return;
            }

            if (plugin.explosionProtectionAll) {
                if (BlockUtil.isInList(block.getTypeId(), BlockUtil.materialListNonDoors)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
