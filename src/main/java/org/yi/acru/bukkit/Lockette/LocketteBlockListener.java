//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//
package org.yi.acru.bukkit.Lockette;
// Imports.
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.ChatColor;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import org.yi.acru.bukkit.PluginCore;
import org.yi.acru.bukkit.BlockUtil;

public class LocketteBlockListener implements Listener {

	private static Lockette plugin;

	public LocketteBlockListener(Lockette instance) {
		plugin = instance;
	}

	protected void registerEvents() {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(this, plugin);
	}

	//**********************************************************
	// Start of event section

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMoveItemEvent(InventoryMoveItemEvent event) {
        Boolean protectedSource = false;
        Boolean protectedDest = false;
        Boolean protectedDoubleChestLeft = false;
        Boolean protectedDoubleChestRight = false;
        if ((event.getSource().getHolder()) instanceof BlockState)
            protectedSource = (Lockette.isProtected(((BlockState) event.getSource().getHolder()).getBlock()));
        if ((event.getDestination().getHolder()) instanceof BlockState)
            protectedDest = (Lockette.isProtected(((BlockState) event.getDestination().getHolder()).getBlock()));
        if (protectedSource != protectedDest) {
            event.setCancelled(true);
            return;//Booya thats like 90% of the noise...
        }
        
        //  Handle DoubleChests here now.. not completed but getting close... BROKEN SO FAR...
        /*if ((event.getSource().getHolder()) instanceof DoubleChest) {
            if (Lockette.isProtected(((BlockState)(((DoubleChest) event.getSource()).getLeftSide().getInventory().getHolder())).getBlock())) {
                // protectedDoubleChestLeft = ((BlockState)(((DoubleChest) event.getSource()).getLeftSide().getInventory().getHolder())).getBlock();
                Lockette.log.info("Okay we are checking double chests now... left side says, " + protectedDoubleChestLeft);
            }
            if (Lockette.isProtected(((BlockState)(((DoubleChest) event.getSource()).getRightSide().getInventory().getHolder())).getBlock())) {
                // protectedDoubleChestLeft = ((BlockState)(((DoubleChest) event.getSource()).getLeftSide().getInventory().getHolder())).getBlock();
                Lockette.log.info("Okay we are checking double chests now... right side says, " + protectedDoubleChestRight);
            }
        }*/
        //  And now check for minecarts last because they cant be protected.
        if (event.getSource().getHolder() instanceof HopperMinecart & protectedDest) {
            //Lockette.log.info("Canceling Minecart + protectedDest:" + protectedDest);
            event.setCancelled(true);
            return;
        }
        else if (event.getDestination().getHolder() instanceof HopperMinecart & protectedSource) {
            //Lockette.log.info("Canceling Minecart + protectedSource:" + protectedSource);
            event.setCancelled(true);
            return;
        }
        else if (event.getDestination().getHolder() instanceof HopperMinecart & protectedSource) {
            //Lockette.log.info("Canceling Minecart + pSrc:" + pSrc);
            event.setCancelled(true);
            return;
        }
        else if (event.getSource().getHolder() instanceof StorageMinecart & protectedDest) {
            //Lockette.log.info("Canceling Minecart + protectedDest:" + protectedDest);
            event.setCancelled(true);
            return;
        }
        else if (event.getDestination().getHolder() instanceof StorageMinecart & protectedSource) {
            //Lockette.log.info("Canceling Minecart + protectedSource:" + protectedSource);
            event.setCancelled(true);
            return;
        }
        else if (event.getDestination().getHolder() instanceof StorageMinecart & protectedSource) {
            //Lockette.log.info("Canceling Minecart + pSrc:" + pSrc);
            event.setCancelled(true);
        }
    }

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {

		Player player = event.getPlayer();
		Block block = event.getBlock();
		int type = block.getTypeId();

		if (event.isCancelled())
			if (!BlockUtil.isInList(type, BlockUtil.materialListJustDoors))
				return;

		// Someone is breaking a block, lets see if they are allowed.
		if (type == Material.WALL_SIGN.getId()) {
			if (block.getData() == 0) {
				// Fix for mcMMO error.
				block.setData((byte) 5);
			}

			Sign sign = (Sign) block.getState();
			String text = ChatColor.stripColor(sign.getLine(0));
			
			if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(Lockette.altPrivate)) {
				int length = player.getName().length();

				// Check owner.
				//if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
				if (Lockette.isOwner(sign, player)) {
					//Block		checkBlock = Lockette.getSignAttachedBlock(block);
					//if(checkBlock == null) checkBlock = block;

					//if((checkBlock.getTypeId() != Material.WOODEN_DOOR.getId()) && (checkBlock.getTypeId() != Material.IRON_DOOR_BLOCK.getId())){
					Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has released a container.");
					//}
					//else Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has released a door.");
					Lockette.removeUUIDMetadata(sign);
					
					plugin.localizedMessage(player, null, "msg-owner-release");
					return;
				}

				// At this point, check admin.

				if (Lockette.adminBreak) {
					boolean snoop = false;

					if (plugin.hasPermission(block.getWorld(), player, "lockette.admin.break"))
						snoop = true;

					if (snoop) {
						Lockette.log.info("[" + plugin.getDescription().getName() + "] (Admin) " + player.getName() + " has broken open a container owned by " + sign.getLine(1) + "!");

						Lockette.removeUUIDMetadata(sign);
						plugin.localizedMessage(player, Lockette.broadcastBreakTarget, "msg-admin-release", sign.getLine(1));
						return;
					}
				}

				event.setCancelled(true);
				sign.update();

				plugin.localizedMessage(player, null, "msg-user-release-owned", sign.getLine(1));
			} else if (text.equalsIgnoreCase("[More Users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
				Block checkBlock = Lockette.getSignAttachedBlock(block);
				if (checkBlock == null)
					return;

				Block signBlock = Lockette.findBlockOwner(checkBlock);
				if (signBlock == null)
					return;

				Sign sign2 = (Sign) signBlock.getState();
				if (Lockette.isOwner(sign2, player)) {
					Lockette.removeUUIDMetadata(sign);
					plugin.localizedMessage(player, null, "msg-owner-remove");
					return;
				}

				event.setCancelled(true);
				sign.update();

				plugin.localizedMessage(player, null, "msg-user-remove-owned", sign2.getLine(1));
			}
		} else {
			Block signBlock = Lockette.findBlockOwner(block);
			
			if (signBlock == null)
				return;

			Sign sign = (Sign) signBlock.getState();
			// Check owner.
			if (Lockette.isOwner(sign, player)) {
				signBlock = Lockette.findBlockOwnerBreak(block);
				if (signBlock != null) {
					// This block has the sign attached.  (Or the the door above the block.)
					sign = (Sign) signBlock.getState();
					Lockette.removeUUIDMetadata(sign);
					
					Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has released a container.");
				} else {
					// Partial release for chest/doors, the sign may now be invalid for doors, but is always valid for chests.

					if (BlockUtil.isInList(type, BlockUtil.materialListJustDoors)) {
						// Check for invalid signs somehow?
						// But valid signs can be collided anyways... so probably doesn't matter.  (Unless this is prevented too.)
					}
				}
				return;
			}

			event.setCancelled(true);
			//if(!Lockette.enhancedEvents){
			//	// Fix for broken doors in build xxx-560.
			//	if(type == Material.WOODEN_DOOR.getId()) Lockette.toggleSingleDoor(block);
			//}

			plugin.localizedMessage(player, null, "msg-user-break-owned", sign.getLine(1));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {

		Block block = event.getBlock();

		// Check the block list for any protected blocks, and cancel the event if any are found.

		Block checkBlock;
		List<Block> blockList = event.getBlocks();
		int x, count = blockList.size();

		for (x = 0; x < count; ++x) {
			checkBlock = blockList.get(x);

			if (Lockette.isProtected(checkBlock)) {
				event.setCancelled(true);
				return;
			}
		}

		// The above misses doors at the end of the chain, in the space the blocks are being pushed into.

		checkBlock = block.getRelative(Lockette.getPistonFacing(block), event.getLength() + 1);

		if (Lockette.isProtected(checkBlock)) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {

		if (!(event.isSticky()))
			return;

		Block block = event.getBlock();
		Block checkBlock = block.getRelative(Lockette.getPistonFacing(block), 2);
		//Block		checkBlock = event.getRetractLocation().getBlock();
		int type = checkBlock.getTypeId();

		// Skip those mats that cannot be pulled.

		if (BlockUtil.isInList(type, BlockUtil.materialListNonDoors))
			return;
		if (BlockUtil.isInList(type, BlockUtil.materialListJustDoors))
			return;
		//if(type == Material.TRAP_DOOR.getId()) don't return

		if (Lockette.isProtected(checkBlock))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		Block block = event.getBlockPlaced();
		int type = block.getTypeId();
		Block against = event.getBlockAgainst();
		Block checkBlock;
		Block signBlock;

		// Check if someone accidentally put any block on an owned sign.

		if (against.getTypeId() == Material.WALL_SIGN.getId()) {
			// Only cancel it for our signs.
			Sign sign = (Sign) against.getState();
			String text = ChatColor.stripColor(sign.getLine(0));

			if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(Lockette.altPrivate) || text.equalsIgnoreCase("[More Psers]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
				event.setCancelled(true);
				return;
			}
		}

		// Check the placing of a door by a door here.
		// Though it is usually an item, not a block?  Is this still needed?

		if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
			//player.sendMessage(ChatColor.DARK_PURPLE + "Lockette: Door block block has been placed");

			if (canBuildDoor(block, against, player))
				return;

			event.setCancelled(true);

			plugin.localizedMessage(player, null, "msg-user-conflict-door");
			return;
		}

		if (Lockette.directPlacement) {
			if (type == Material.WALL_SIGN.getId()) {
				checkBlock = Lockette.getSignAttachedBlock(block);
				
				if (checkBlock == null)
					return;

				type = checkBlock.getTypeId();

				if (BlockUtil.isInList(type, BlockUtil.materialListNonDoors)
					|| Lockette.isInList(type, Lockette.customBlockList)) {

					Sign sign = (Sign) block.getState();

					if (Lockette.isProtected(checkBlock)) {
						// Add a users sign only if owner.
						if (Lockette.isOwner(checkBlock, player)) {
							sign.setLine(0, Lockette.altMoreUsers);
							sign.setLine(1, Lockette.altEveryone);
							sign.setLine(2, "");
							sign.setLine(3, "");
							sign.update(true);
	
							plugin.localizedMessage(player, null, "msg-owner-adduser");
						} else
							event.setCancelled(true);
							
						return;
					} else {
						// Check for permission first.
						if (!checkPermissions(player, block, checkBlock)) {
							event.setCancelled(true);
							plugin.localizedMessage(player, null, "msg-error-permission");
							return;
						}
	
						sign.setLine(0, Lockette.altPrivate);
						Lockette.setLine(sign, 1, player.getName());
						
						sign.setLine(2, "");
						sign.setLine(3, "");
						sign.update(true);
	
						Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has protected a block or door.");
						
						plugin.localizedMessage(player, null, "msg-owner-claim");
					}
				}

				return;
			}
		}

		// The rest is for placing chests and hoppers only.		
		if (BlockUtil.isInList(type, BlockUtil.materialListChests)) {
			// Count nearby chests to find illegal sized chests.
			
			int chests = Lockette.findChestCountNear(block);

			if (chests > 1) {
				event.setCancelled(true);

				plugin.localizedMessage(player, null, "msg-user-illegal");
				return;
			}

			signBlock = Lockette.findBlockOwner(block);

			if (signBlock != null) {
				// Expanding a private chest, see if its allowed.
				
				Sign sign = (Sign) signBlock.getState();
				// Check owner.
				if (Lockette.isOwner(sign, player)) 
					return;

				// If we got here, then not allowed.

				event.setCancelled(true);

				plugin.localizedMessage(player, null, "msg-user-resize-owned", sign.getLine(1));
			} else {
				// Only send one helpful message per user per session.

				if (plugin.playerList.get(player.getName()) == null) {
					// Associate the user with a non-null block, and print a helpful message.
					plugin.playerList.put(player.getName(), block);
					plugin.localizedMessage(player, null, "msg-help-chest");
				}
			}
		}

		// Hoppers from here.

		if (type == Material.HOPPER.getId()) {

			checkBlock = block.getRelative(BlockFace.UP);
			type = checkBlock.getTypeId();

			if (BlockUtil.isInList(type, BlockUtil.materialListNonDoors) ||
				Lockette.isInList(type, Lockette.customBlockList)) {

				if (!validateOwner(checkBlock, player)) {

					event.setCancelled(true);

					plugin.localizedMessage(player, null, "msg-user-denied");
					return;
				}
			}

			checkBlock = block.getRelative(BlockFace.DOWN);
			type = checkBlock.getTypeId();

			if (BlockUtil.isInList(type, BlockUtil.materialListNonDoors) ||
				Lockette.isInList(type, Lockette.customBlockList)) {

				if (!validateOwner(checkBlock, player)) {

					event.setCancelled(true);

					plugin.localizedMessage(player, null, "msg-user-denied");
					return;
				}
			}

		}

	}
	
	/**
	 * Check permissions and external sources to see if we are allowed to place a private sign here
	 * 
	 * @return true if permitted
	 */
	private boolean checkPermissions(Player player, Block block, Block checkBlock) {
		int type = checkBlock.getTypeId();
		
		if (plugin.usingExternalZones()) {
			if (!plugin.canBuild(player, block)) {

				plugin.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
				return false;
			}

			if (!plugin.canBuild(player, checkBlock)) {

				plugin.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
				return false;
			}
		}

		if (plugin.usingExternalPermissions()) {
			boolean create = false;

			if (plugin.hasPermission(block.getWorld(), player, "lockette.create.all"))
				create = true;
			else if (BlockUtil.isInList(type, BlockUtil.materialListChests)) {
				if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.chest"))
					create = true;
			} else if ((type == Material.FURNACE.getId()) || (type == Material.BURNING_FURNACE.getId())) {
				if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.furnace"))
					create = true;
			} else if (type == Material.DISPENSER.getId()) {
				if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dispenser"))
					create = true;
			} else if (type == Material.DROPPER.getId()) {
				if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dropper"))
					create = true;
			} else if (type == Material.BREWING_STAND.getId()) {
				if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.brewingstand"))
					create = true;
			} else if (Lockette.isInList(type, Lockette.customBlockList)) {
				if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.custom"))
					create = true;
			}

			return create;
		}
		
		return true;
	}

	/**
	 * Check for a private sign and check we are the owner of this block.
	 * 
	 * @param block
	 * @param player
	 * @return true if no owner or we are the owner named on the private sign.
	 */
	private boolean validateOwner(Block block, Player player) {
		Block signBlock = Lockette.findBlockOwner(block);
		
		// No sign block so has no owner.
		if (signBlock == null)
			return true;

		Sign sign = (Sign) signBlock.getState();

		// Check owner.
		if (Lockette.isOwner(sign, player))
			return true;

		// Owner doesn't match so deny.
		return false;
	}

	/*
	 * BukkitMulti public void onBlockRightClick(BlockRightClickEvent event){
	 * Block block = event.getBlock(); int type = block.getTypeId();
	 * 
	 * 
	 * // Right clicking a door here.
	 * 
	 * if(Lockette.protectTrapDoors) if(type == Material.TRAP_DOOR.getId()){
	 * Player player = event.getPlayer();
	 * 
	 * rightclickDoor(block, player); return; }
	 * 
	 * if(Lockette.protectDoors) if((type == Material.WOODEN_DOOR.getId()) ||
	 * (type == Material.IRON_DOOR_BLOCK.getId()) || (type ==
	 * Material.FENCE_GATE.getId())){ Player player = event.getPlayer();
	 * 
	 * rightclickDoor(block, player); return; }
	 * 
	 * 
	 * // The rest is for wall signs only.
	 * 
	 * if(type != Material.WALL_SIGN.getId()) return;
	 * 
	 * Player player = event.getPlayer();
	 * 
	 * rightclickSign(block, player); }
	 */

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		
		Block block = event.getBlock();
		int type = block.getTypeId();
		boolean doCheck = false;
		
		if (Lockette.protectTrapDoors) {
			if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors))
				doCheck = true;
		}
		
		if (Lockette.protectDoors) {
			if (BlockUtil.isInList(type, BlockUtil.materialListDoors))
				doCheck = true;
		}
		
		if (doCheck) {
			// Lets see if everyone is allowed to activate.
			Block signBlock = Lockette.findBlockOwner(block);
			
			if (signBlock == null)
				return;
			
			// Check main three users.
			
			Sign sign = (Sign) signBlock.getState();
			String line;
			
			for (int y = 1; y <= 3; ++y)
				if (!sign.getLine(y).isEmpty()) {
					line = ChatColor.stripColor(sign.getLine(y));
					
					if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(Lockette.altEveryone))
						return;
				}
			
			// Check for more users.
			
			List<Block> list = Lockette.findBlockUsers(block, signBlock);
			for (Block blk : list) {
				sign = (Sign) blk.getState();
				
				for (int y = 1; y <= 3; ++y)
					if (!sign.getLine(y).isEmpty()) {
						line = ChatColor.stripColor(sign.getLine(y));
						
						if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(Lockette.altEveryone))
							return;
					}
			}
			
			// Don't have permission.
			
			event.setNewCurrent(event.getOldCurrent());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onSignChange(SignChangeEvent event) {

		Player player = event.getPlayer();
		Block block = event.getBlock();
		int blockType = block.getTypeId();
		boolean typeWallSign = (blockType == Material.WALL_SIGN.getId());
		boolean typeSignPost = (blockType == Material.SIGN_POST.getId());

		/*
		 * // Check to see if it is a sign change packet for an existing
		 * protected sign. // No longer needed in builds around 556+, but I am
		 * leaving this here for now. // Needed again as of build 1093... :< //
		 * Moved to PrefixListener
		 * 
		 * if(typeWallSign){ Sign sign = (Sign) block.getState(); String text =
		 * sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "");
		 * 
		 * if(text.equalsIgnoreCase("[Private]") ||
		 * text.equalsIgnoreCase(Lockette.altPrivate) ||
		 * text.equalsIgnoreCase("[More Users]") ||
		 * text.equalsIgnoreCase(Lockette.altMoreUsers)){
		 * //if(!sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").isEmpty()){
		 * // Need to set canceled to false and set event text, as the sign is
		 * cleared otherwise. event.setCancelled(false); event.setLine(0,
		 * sign.getLine(0)); event.setLine(1, sign.getLine(1)); event.setLine(2,
		 * sign.getLine(2)); event.setLine(3, sign.getLine(3));
		 * Lockette.log.info("[" + plugin.getDescription().getName() + "] " +
		 * player.getName() +
		 * " just tried to change a non-editable sign. (Bukkit bug, or plugin conflict?  Disable MinecartManiaAdminControls.)"
		 * ); return; } }
		 */

		// But also need this along with stuff in PrefixListener
		
		if (typeWallSign) {
			Sign sign = (Sign) block.getState();
			String text = ChatColor.stripColor(sign.getLine(0));

			if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(Lockette.altPrivate) || text.equalsIgnoreCase("[More Users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
				if (event.isCancelled())
					return;
				//event.setCancelled(true);
				//return;
			}
		} else if (typeSignPost) {

		} else {
			// Not a sign, wtf!
			event.setCancelled(true);
			return;
		}

		/*
		 * // Alternative: Enforce a blank sign, as bukkit catches spoofed
		 * packets now. // No longer needed, as the findOwner now has an ignore
		 * block.
		 * 
		 * if(typeWallSign || (block.getTypeId() ==
		 * Material.SIGN_POST.getId())){ Sign sign = (Sign) block.getState();
		 * String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]",
		 * "").toLowerCase();
		 * 
		 * if(text.equals("[private]") || text.equals(Lockette.altPrivate) ||
		 * text.equals("[more users]") || text.equals(Lockette.altMoreUsers)){
		 * sign.setLine(0, ""); sign.setLine(1, ""); sign.setLine(2, "");
		 * sign.setLine(3, ""); sign.update(true); } }
		 */
		/*
		 * // Colorizer code. // Moved to PrefixListener
		 * 
		 * if(Lockette.colorTags){ event.setLine(0,
		 * event.getLine(0).replaceAll("&([0-9A-Fa-f])", "\u00A7$1"));
		 * event.setLine(1, event.getLine(1).replaceAll("&([0-9A-Fa-f])",
		 * "\u00A7$1")); event.setLine(2,
		 * event.getLine(2).replaceAll("&([0-9A-Fa-f])", "\u00A7$1"));
		 * event.setLine(3, event.getLine(3).replaceAll("&([0-9A-Fa-f])",
		 * "\u00A7$1")); }
		 */

		// Check for a new [Private] or [More Users] sign.
		String text = ChatColor.stripColor(event.getLine(0));
		
		if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(Lockette.altPrivate)) {
			//Player		player = event.getPlayer();
			//Block		block = event.getBlock();
			//boolean		typeWallSign = (block.getTypeId() == Material.WALL_SIGN.getId());
			boolean doChests = true, doFurnaces = true, doDispensers = true, doDroppers = true;
			boolean doBrewingStands = true, doCustoms = true;
			boolean doTrapDoors = true, doDoors = true;

			// Check for permission first.
			if (plugin.usingExternalZones()) {
				if (!plugin.canBuild(player, block)) {
					event.setLine(0, "[?]");

					plugin.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
					return;
				}
			}

			if (plugin.usingExternalPermissions()) {
				boolean create = false;

				doChests = false;
				doFurnaces = false;
				doDispensers = false;
				doDroppers = false;
				doBrewingStands = false;
				doCustoms = false;
				doTrapDoors = false;
				doDoors = false;

				if (plugin.hasPermission(block.getWorld(), player, "lockette.create.all")) {
					create = true;
					doChests = true;
					doFurnaces = true;
					doDispensers = true;
					doDroppers = true;
					doBrewingStands = true;
					doCustoms = true;
					doTrapDoors = true;
					doDoors = true;
				} else {
					if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.chest")) {
						create = true;
						doChests = true;
					}
					if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.furnace")) {
						create = true;
						doFurnaces = true;
					}
					if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dispenser")) {
						create = true;
						doDispensers = true;
					}
					if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.dropper")) {
						create = true;
						doDroppers = true;
					}
					if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.brewingstand")) {
						create = true;
						doBrewingStands = true;
					}
					if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.custom")) {
						create = true;
						doCustoms = true;
					}
					if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.trapdoor")) {
						create = true;
						doTrapDoors = true;
					}
					if (plugin.hasPermission(block.getWorld(), player, "lockette.user.create.door")) {
						create = true;
						doDoors = true;
					}
				}

				if (!create) {
					event.setLine(0, "[?]");

					plugin.localizedMessage(player, null, "msg-error-permission");
					return;
				}
			}

			int x;
			Block checkBlock[] = new Block[4];
			byte face = 0;
			int type = 0;
			boolean conflict = false;
			boolean deny = false;
			boolean zonedeny = false;
			
			// Check wall sign attached block for trap doors.
			if (Lockette.protectTrapDoors)
				if (typeWallSign) {
					checkBlock[3] = Lockette.getSignAttachedBlock(block);

					if (checkBlock[3] != null)
						if (!BlockUtil.isInList(checkBlock[3].getTypeId(), BlockUtil.materialListBad)) {
							checkBlock[0] = checkBlock[3].getRelative(BlockFace.NORTH);
							checkBlock[1] = checkBlock[3].getRelative(BlockFace.EAST);
							checkBlock[2] = checkBlock[3].getRelative(BlockFace.SOUTH);
							checkBlock[3] = checkBlock[3].getRelative(BlockFace.WEST);

							for (x = 0; x < 4; ++x) {
								if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListTrapDoors)) {
									if (Lockette.findBlockOwner(checkBlock[x], block, true) == null) {
										if (!doTrapDoors)
											deny = true;
										else {
											face = block.getData();
											type = 4;
											break;
										}
									}
								}
							}
							/*
							 * if(Lockette.findBlockOwner(checkBlock[3], block,
							 * true) == null){ if(!doTrapDoors) deny = true;
							 * else{ face = block.getData(); type = 4; } }
							 */
						}
				}

			// Check wall sign attached block for doors, above and below.
			if (Lockette.protectDoors)
				if (typeWallSign) {
					checkBlock[0] = Lockette.getSignAttachedBlock(block);

					if (checkBlock[0] != null)
						if (!BlockUtil.isInList(checkBlock[0].getTypeId(), BlockUtil.materialListBad)) {
							checkBlock[1] = checkBlock[0].getRelative(BlockFace.UP);
							checkBlock[2] = checkBlock[0].getRelative(BlockFace.DOWN);

							if (BlockUtil.isInList(checkBlock[1].getTypeId(), BlockUtil.materialListDoors)) {
								if (Lockette.findBlockOwner(checkBlock[1], block, true) == null) {
									if (BlockUtil.isInList(checkBlock[2].getTypeId(), BlockUtil.materialListDoors)) {
										if (Lockette.findBlockOwner(checkBlock[2], block, true) == null) {
											// unclaimed (unowned above, unowned below)
											if (!doDoors)
												deny = true;
											else {
												face = block.getData();
												type = 5;
											}
										}
										// else conflict (unowned above, but already owned below)
										else
											conflict = true;
									} else {
										// unclaimed (unowned above, empty below)
										if (!doDoors)
											deny = true;
										else {
											face = block.getData();
											type = 5;
										}
									}
								}
								// else unknown (already owned above, unknown below)
								//else if(isInList(checkBlock[2].getTypeId(), materialListDoors)){
								//if(Lockette.findBlockOwner(checkBlock[2]) == null){
								// conflict (already owned above, but unowned below)
								//conflict = true;
								//}
								//else claimed (+ conflict) (already owned above, already owned below)
								//else 
								//conflict = true;
								//}
								// else claimed (+ conflict obscure) (already owned above, empty below)
								else
									conflict = true;
							} else if (BlockUtil.isInList(checkBlock[2].getTypeId(), BlockUtil.materialListDoors)) {
								if (Lockette.findBlockOwner(checkBlock[2], block, true) == null) {
									// unclaimed (empty above, unowned below)
									if (!doDoors)
										deny = true;
									else {
										face = block.getData();
										type = 5;
									}
								}
								// else claimed (+ conflict) (empty above, already owned below)
								else
									conflict = true;
							}
							// else none (empty above, empty below)
						}
				}

			// Reset trapdoor face if there is a conflict with a door.
			if (conflict == true) {
				face = 0;
				type = 0;
			}

			if (face == 0) {
				int lastType;

				// Check for chests first, dispensers second, furnaces third.
				checkBlock[0] = block.getRelative(BlockFace.NORTH);
				checkBlock[1] = block.getRelative(BlockFace.EAST);
				checkBlock[2] = block.getRelative(BlockFace.SOUTH);
				checkBlock[3] = block.getRelative(BlockFace.WEST);

				for (x = 0; x < 4; ++x) {
					if (plugin.usingExternalZones()) {
						if (!plugin.canBuild(player, checkBlock[x])) {
							zonedeny = true;
							continue;
						}
					}

					// Check if allowed by type.
					if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListChests)) {
						if (!doChests) {
							deny = true;
							continue;
						}
						lastType = 1;
					} else if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListFurnaces)) {
						if (!doFurnaces) {
							deny = true;
							continue;
						}
						lastType = 2;
					} else if (checkBlock[x].getTypeId() == Material.DISPENSER.getId()) {
						if (!doDispensers) {
							deny = true;
							continue;
						}
						lastType = 3;
					} else if (checkBlock[x].getTypeId() == Material.DROPPER.getId()) {
						if (!doDroppers) {
							deny = true;
							continue;
						}
						lastType = 8;
					} else if (checkBlock[x].getTypeId() == Material.BREWING_STAND.getId()) {
						if (!doBrewingStands) {
							deny = true;
							continue;
						}
						lastType = 6;
					} else if (Lockette.isInList(checkBlock[x].getTypeId(), Lockette.customBlockList)) {
						if (!doCustoms) {
							deny = true;
							continue;
						}
						lastType = 7;
						} else if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListTrapDoors)) {
						if (!Lockette.protectTrapDoors)
							continue;
						if (!doTrapDoors) {
							deny = true;
							continue;
						}
						lastType = 4;
					} else if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListDoors)) {
						if (!Lockette.protectDoors)
							continue;
						if (!doDoors) {
							deny = true;
							continue;
						}
						lastType = 5;
					} else
						continue;

					// Allowed, lets see if it is claimed.
					if (Lockette.findBlockOwner(checkBlock[x], block, true) == null) {
						face = BlockUtil.faceList[x];
						type = lastType;
						break;
					}
					// For when the last type is a door, and it is conflicting.
					else {
						if (Lockette.protectTrapDoors) {
							if (doTrapDoors) {
								if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListTrapDoors)) {
									conflict = true;
								}
							}
						}
						if (Lockette.protectDoors) {
							if (doDoors) {
								if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListDoors)) {
									conflict = true;
								}
							}
						}
					}
				}
			}

			// None found, send a message.

			if (face == 0) {
				event.setLine(0, "[?]");

				if (conflict)
					plugin.localizedMessage(player, null, "msg-error-claim-conflict");
				else if (zonedeny)
					plugin.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
				else if (deny)
					plugin.localizedMessage(player, null, "msg-error-permission");
				else
					plugin.localizedMessage(player, null, "msg-error-claim");
				return;
			}

			// Claim it...

			boolean anyone = true;
			if (Lockette.DEBUG) {
				Lockette.log.info("[Lockette] creating new Lockette sign");
				Lockette.log.info("[Lockette] 1st line = " + event.getLine(1));
			}
			
			if (event.getLine(1).isEmpty())
				anyone = false;

			// In case some other plugin messed with the cancel state.
			event.setCancelled(false);

			if (anyone) {
				// Check if allowed by type.
				if (type == 1) {	// Chest
					if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.chest"))
						anyone = false;
				} else if (type == 2) {	// Furnace
					if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.furnace"))
						anyone = false;
				} else if (type == 3) {	// Dispenser
					if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.dispenser"))
						anyone = false;
				} else if (type == 8) {	// Dropper
					if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.dropper"))
						anyone = false;
				} else if (type == 6) {	// Brewing Stand
					if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.brewingstand"))
						anyone = false;
				} else if (type == 7) {	// Custom
					if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.custom"))
						anyone = false;
				} else if (type == 4) {	// Trap Door
					if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.trapdoor"))
						anyone = false;
				} else if (type == 5) {	// Door
					if (!plugin.hasPermission(block.getWorld(), player, "lockette.admin.create.door"))
						anyone = false;
				} else
					anyone = false;
			}
 
			if (!anyone) {
				// Re-set the text.
				Sign sign = (Sign) block.getState();
				if (Lockette.DEBUG) {
					Lockette.log.info("[Lockette] Setting palyer's name : " + player.getName());
				}
				Lockette.setLine(sign, 1, player.getName());
				event.setLine(1, player.getName());				
				sign.update(true);
			} else { 			// addming creating a sign for someone else.
				Sign sign = (Sign) block.getState();
				if (Lockette.DEBUG) {
					Lockette.log.info("[Lockette] Setting other's name : " + event.getLine(1));
				}
				Lockette.setLine(sign, 1, event.getLine(1));
				event.setLine(1, event.getLine(1));	
			}

			// this is fishy...
			if (!typeWallSign) {
				// Set to wall type.
				block.setType(Material.WALL_SIGN);
				block.setData(face);

				// Re-set the text.
				Sign sign = (Sign) block.getState();
				
				sign.setLine(0, event.getLine(0));
				Lockette.setLine(sign, 1, event.getLine(1));
				Lockette.setLine(sign, 2, event.getLine(2));
				Lockette.setLine(sign, 3, event.getLine(3));				
				sign.update(true);
			} else
				block.setData(face);

			// All done!

			if (anyone) {
				Lockette.log.info("[" + plugin.getDescription().getName() + "] (Admin) " + player.getName() + " has claimed a container for " + event.getLine(1) + ".");

				if (!plugin.playerOnline(event.getLine(1)))
					plugin.localizedMessage(player, null, "msg-admin-claim-error", event.getLine(1));
				else
					plugin.localizedMessage(player, null, "msg-admin-claim", event.getLine(1));
			} else {
				Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has claimed a container.");

				plugin.localizedMessage(player, null, "msg-owner-claim");
			}
		} else if (text.equalsIgnoreCase("[More Users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
			//Player		player = event.getPlayer();
			//Block		block = event.getBlock();
			//boolean		typeWallSign = (block.getTypeId() == Material.WALL_SIGN.getId());
			
			int x;
			Block checkBlock[] = new Block[4];
			Block signBlock = null;
			Sign sign = null;
			byte face = 0;

			// Check wall sign attached block for owner.
			if (Lockette.protectDoors || Lockette.protectTrapDoors) {
				if (typeWallSign) {
					checkBlock[0] = Lockette.getSignAttachedBlock(block);
					
					if (checkBlock[0] != null) {
						if (!BlockUtil.isInList(checkBlock[0].getTypeId(), BlockUtil.materialListBad)) {
							signBlock = Lockette.findBlockOwner(checkBlock[0]);
							
							if (signBlock != null) {
								sign = (Sign) signBlock.getState();
								
								// Check owner.
								if (Lockette.isOwner(sign, player)) {
									face = block.getData();
								}
							}
						}
					}
				}
			}
			
			if (face == 0) {
				// Check for chests first, dispensers second, furnaces third.

				checkBlock[0] = block.getRelative(BlockFace.NORTH);
				checkBlock[1] = block.getRelative(BlockFace.EAST);
				checkBlock[2] = block.getRelative(BlockFace.SOUTH);
				checkBlock[3] = block.getRelative(BlockFace.WEST);
				
				for (x = 0; x < 4; ++x) {
					if (!BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialList))
						continue;

					if (!Lockette.protectTrapDoors) {
						if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListTrapDoors))
							continue;
					}
					
					if (!Lockette.protectDoors) {
						if (BlockUtil.isInList(checkBlock[x].getTypeId(), BlockUtil.materialListDoors))
							continue;
					}
					
					signBlock = Lockette.findBlockOwner(checkBlock[x]);
					
					if (signBlock != null) {
						sign = (Sign) signBlock.getState();
						
						// Check owner.
						if (Lockette.isOwner(sign, player)) {
							face = BlockUtil.faceList[x];
							//type = y;
							break;
						}
					}
				}
			}
			
			// None found, send a message.
			
			if (face == 0) {
				event.setLine(0, "[?]");
				if (sign != null) {
					plugin.localizedMessage(player, null, "msg-error-adduser-owned", sign.getLine(1));
				} else {
					plugin.localizedMessage(player, null, "msg-error-adduser");
				}
				return;
			}

			// Add the users sign.

			// In case some other plugin messed with the cancel state.
			event.setCancelled(false);
			if (!typeWallSign) {
				// Set to wall type.
				block.setType(Material.WALL_SIGN);
				block.setData(face);

				// Re-set the text.
				//Sign		
				sign = (Sign) block.getState();

				sign.setLine(0, event.getLine(0));
				Lockette.setLine(sign, 1, event.getLine(1));
				Lockette.setLine(sign, 2, event.getLine(2));
				Lockette.setLine(sign, 3, event.getLine(2));				

				sign.update(true);
				
			} else
				block.setData(face);
			
			// All done!
			
			plugin.localizedMessage(player, null, "msg-owner-adduser");
		}
	}


	//**********************************************************************
	// Start of utility section

	// Returns true if it should be allowed, false if it should be canceled.
	private static boolean canBuildDoor(Block block, Block against, Player player) {

		Block checkBlock;
		//Sign		sign;
		//int			length = player.getName().length();

		//if(length > 15) length = 15;

		// Check block below for doors or block to side for trapdoors.

		//if (!Lockette.isOwner(against, player.getName()))
		if (!Lockette.isOwner(against, player))			
			return (false);

		if (Lockette.protectTrapDoors)
			if (BlockUtil.isInList(block.getTypeId(), BlockUtil.materialListTrapDoors)) {
				//if(!Lockette.isOwner(Lockette.getTrapDoorAttachedBlock(block), player.getName())) return(false);
				//if(!Lockette.isOwner(block, player.getName())) return(false); // Failed as block data is bad, same as above.
				//if(!Lockette.isOwner(against, player.getName())) return(false);
				return (true);
			}

		// Check block above door.

		if (!Lockette.isOwner(against.getRelative(BlockFace.UP, 3), player))			
			return (false);

		// Check neighboring doors.

		checkBlock = block.getRelative(BlockFace.NORTH);
		if (checkBlock.getTypeId() == block.getTypeId()) {
			if (!Lockette.isOwner(checkBlock, player))
				return (false);
		}

		checkBlock = block.getRelative(BlockFace.EAST);
		if (checkBlock.getTypeId() == block.getTypeId()) {
			if (!Lockette.isOwner(checkBlock, player))
				return (false);
		}

		checkBlock = block.getRelative(BlockFace.SOUTH);
		if (checkBlock.getTypeId() == block.getTypeId()) {
			if (!Lockette.isOwner(checkBlock, player))
				return (false);
		}

		checkBlock = block.getRelative(BlockFace.WEST);
		if (checkBlock.getTypeId() == block.getTypeId()) {
			if (!Lockette.isOwner(checkBlock, player))
				return (false);
		}

		return (true);
	}
}
