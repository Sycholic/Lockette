//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//
package org.yi.acru.bukkit.Lockette.Listeners;

import java.util.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityExplodeEvent;
import org.yi.acru.bukkit.BlockUtil;
import org.yi.acru.bukkit.Lockette.Lockette;
import org.yi.acru.bukkit.Lockette.LocketteAPI;

public class LocketteEntityListener implements Listener {

    private final Lockette plugin;
    private final LocketteAPI locketteAPI;

    public LocketteEntityListener(Lockette instance) {
        plugin = instance;
        locketteAPI = plugin.locketteAPI;
    }

    // Start of event section
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Check the block list for any protected blocks, and cancel the event if any are found.
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (locketteAPI.isProtected(block)) {
                it.remove();
                continue;
            }
            if (plugin.explosionProtectionAll) {
                if (BlockUtil.isInList(block.getTypeId(), BlockUtil.materialListNonDoors)) {
                    it.remove();
                }
            }
        }
    }
}
