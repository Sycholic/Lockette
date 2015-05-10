//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//
package org.yi.acru.bukkit.Lockette;

import java.util.UUID;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.PluginManager;
import org.yi.acru.bukkit.BlockUtil;

public class LocketteInventoryListener implements Listener {

    private final Lockette plugin;
    public LocketteInventoryListener(Lockette instance) {
        this.plugin = instance;
    }

    //**************************************************************************
    // Start of event section
    private boolean isProtected(Inventory inv, boolean allowEveryone) {
        if (!plugin.blockHopper) {
            return false;
        }

        InventoryHolder holder = inv.getHolder();

        if (holder instanceof DoubleChest) {
            holder = ((DoubleChest) holder).getLeftSide();
        }

        if (holder instanceof BlockState) {
            Block block = ((BlockState) holder).getBlock();
            int type = block.getTypeId();
            if (BlockUtil.isInList(type, BlockUtil.materialListNonDoors)
                    || plugin.isInList(type, plugin.customBlockList)) {
                return (allowEveryone && plugin.isEveryone(block)) ? false : plugin.isProtected(block);
            }
        }
        return false;
    }

    private boolean passThrough(Inventory src, Inventory dest, Inventory me) {
        if (!plugin.blockHopper) {
            return true;
        }
        UUID srcOwner = null;
        UUID destOwner = null;
        UUID meOwner = null;

        if (src != null) {
            InventoryHolder holder = src.getHolder();
            if (holder instanceof DoubleChest) {
                holder = ((DoubleChest) holder).getLeftSide();
            }

            if (holder instanceof BlockState) {
                Block block = ((BlockState) holder).getBlock();
                srcOwner = plugin.getProtectedOwnerUUID(block);
                if (plugin.isEveryone(block)) {
                    srcOwner = null;
                }
            }
        }
        if (dest != null) {
            InventoryHolder holder = dest.getHolder();
            if (holder instanceof DoubleChest) {
                holder = ((DoubleChest) holder).getLeftSide();
            }

            if (holder instanceof BlockState) {
                Block block = ((BlockState) holder).getBlock();
                destOwner = plugin.getProtectedOwnerUUID(block);
                if (plugin.isEveryone(block)) {
                    destOwner = null;
                }
            }
        }

        if (me != null) {
            InventoryHolder holder = me.getHolder();
            if (holder instanceof DoubleChest) {
                holder = ((DoubleChest) holder).getLeftSide();
            }

            if (holder instanceof BlockState) {
                Block block = ((BlockState) holder).getBlock();
                meOwner = plugin.getProtectedOwnerUUID(block);
                if (plugin.isEveryone(block)) {
                    meOwner = null;
                }
            }
        }
        return (srcOwner == meOwner & meOwner == destOwner)
                || ((srcOwner == null) && (destOwner == null));
                //|| ((srcOwner == meOwner) && (destOwner == null))
                //|| ((srcOwner == null) && (meOwner == destOwner));
                //why would we allow one or the other be protected? that
                //kind of makes the whole protection moot if you allow it all... derp!
    }

    @EventHandler
    public void onInventoryItemMove(InventoryMoveItemEvent event) {
        Inventory src = event.getSource();
        Inventory dest = event.getDestination();
        Inventory me = event.getInitiator();
        if (passThrough(src, dest, me)) {
            return;
        }
        if (isProtected(event.getSource(), true)
                || isProtected(event.getDestination(), true)) {
            event.setCancelled(true);
        }
    }
}
