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

    private Lockette plugin;
    public LocketteInventoryListener(Lockette instance) {
        this.plugin = instance;
    }

    protected void registerEvents() {
        PluginManager pm = this.plugin.getServer().getPluginManager();
        pm.registerEvents(this, this.plugin);
    }

    //**************************************************************************
    // Start of event section
    private boolean isProtected(Inventory inv, boolean allowEveryone) {
        //plugin.log.info("35:isProtected( " + inv + " - " + allowEveryone);
        if (!plugin.blockHopper) {
            return false;
        }

        InventoryHolder holder = inv.getHolder();

        if (holder instanceof DoubleChest) {
            //plugin.log.info("43:instanceof DoubleChest Logic true");
            holder = ((DoubleChest) holder).getLeftSide();
        }

        if (holder instanceof BlockState) {
            //plugin.log.info("48:instanceof BlockState Logic true");
            Block block = ((BlockState) holder).getBlock();
            int type = block.getTypeId();
            if (BlockUtil.isInList(type, BlockUtil.materialListNonDoors)
                    || plugin.isInList(type, plugin.customBlockList)) {
                //plugin.log.info("53:isInList logic true");
                return (allowEveryone && plugin.isEveryone(block)) ? false : plugin.isProtected(block);
            }
        }
        //plugin.log.info("58:isProtected returning false.");
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
        //plugin.log.info("passThrough(" + srcOwner + " : " + meOwner + " : " + destOwner + ")");
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
        //plugin.log.info("onInventoryItemMove event called");
        if (passThrough(src, dest, me)) {
            //plugin.log.info("passThrough is true.");
            return;
        }
        if (isProtected(event.getSource(), true)
                || isProtected(event.getDestination(), true)) {
            //plugin.log.info("isProtected() of onInventoryItemMove event is canceling");
            event.setCancelled(true);
        }
    }
}
