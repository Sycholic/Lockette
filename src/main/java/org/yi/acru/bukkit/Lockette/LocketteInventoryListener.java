//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//


package org.yi.acru.bukkit.Lockette;

import java.util.*;
import org.bukkit.block.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.*;

import org.yi.acru.bukkit.BlockUtil;

public class LocketteInventoryListener implements Listener{
	private static Lockette		plugin;
	
	public LocketteInventoryListener(Lockette instance){
		this.plugin = instance;
	}
	
	protected void registerEvents(){
		PluginManager	pm = this.plugin.getServer().getPluginManager();

		pm.registerEvents(this, this.plugin);
	}
	
	
	//**************************************************************************
	// Start of event section

	private boolean isProtected(Inventory inv, boolean allowEveryone) {
		if (!Lockette.blockHopper)
			return false;
		
		InventoryHolder holder = inv.getHolder();

		if (holder instanceof DoubleChest) {
			holder = ((DoubleChest) holder).getLeftSide();
		}
		
		if (holder instanceof BlockState) {
			Block block = ((BlockState) holder).getBlock();
			int type = block.getTypeId();
			if (BlockUtil.isInList(type, BlockUtil.materialListNonDoors)
				|| Lockette.isInList(type, Lockette.customBlockList)) {
				return (allowEveryone && Lockette.isEveryone(block)) ? false : Lockette.isProtected(block);
			}
		}
		
		return false;
	}

	private boolean passThrough(Inventory src, Inventory dest, Inventory me) {
		if (!Lockette.blockHopper)
			return true;
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
				srcOwner = Lockette.getProtectedOwnerUUID(block);
				if (Lockette.isEveryone(block))
					srcOwner = null;
			}
		}
		if (dest != null) {
			InventoryHolder holder = dest.getHolder();
			if (holder instanceof DoubleChest) {
				holder = ((DoubleChest) holder).getLeftSide();
			}
			
			if (holder instanceof BlockState) {
				Block block = ((BlockState) holder).getBlock();
				destOwner = Lockette.getProtectedOwnerUUID(block);
				if (Lockette.isEveryone(block))
					destOwner = null;
			}
		}

		if (me != null) {
			InventoryHolder holder = me.getHolder();
			if (holder instanceof DoubleChest) {
				holder = ((DoubleChest) holder).getLeftSide();
			}
			
			if (holder instanceof BlockState) {
				Block block = ((BlockState) holder).getBlock();
				meOwner = Lockette.getProtectedOwnerUUID(block);
				if (Lockette.isEveryone(block))
					meOwner = null;
			}
		}

		if (((srcOwner == meOwner) && (meOwner == destOwner)) ||
			((srcOwner == meOwner) && (destOwner == null)) ||
			((srcOwner == null) && (meOwner == destOwner)) ||
			((srcOwner == null) && (destOwner == null)))
			return true;

		return false;
	}
	
	@EventHandler
	public void onInventoryItemMove(InventoryMoveItemEvent event) {
		Inventory src = event.getSource();
		Inventory dest = event.getDestination();
		Inventory me = event.getInitiator();

		if (passThrough(src, dest, me)) {
			return;
		} 
		if (isProtected(event.getSource(), false) || 
			isProtected(event.getDestination(), true)) {
			event.setCancelled(true);
		}
	}
}

