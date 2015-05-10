//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//
package org.yi.acru.bukkit.Lockette;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.yi.acru.bukkit.BlockUtil;

public class LockettePlayerListener implements Listener {

    private static Lockette plugin;

    public LockettePlayerListener(Lockette instance) {
        plugin = instance;
    }

    // Start of event section
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String[] command = event.getMessage().split(" ", 3);

        if (command.length < 1) {
            return;
        }
        if (!(command[0].equalsIgnoreCase("/lockette") || command[0].equalsIgnoreCase("/lock"))) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();

        // Reload config files, for admins only.
        if (command.length == 2) {
            if (command[1].equalsIgnoreCase("reload")) {
                if (!plugin.hasPermission(player.getWorld(), player, "lockette.admin.reload")) {
                    return;
                }

                plugin.getLocketteProperties().loadProperties(true);

                plugin.messageUtils.localizedMessage(player, plugin.broadcastReloadTarget, "msg-admin-reload");
                return;
            }

            if (command[1].equalsIgnoreCase("version")) {
                player.sendMessage(ChatColor.RED + "Lockette version " + plugin.getDescription().getVersion() + " loaded.  (Core: " + Lockette.getCoreVersion() + ")");
                return;
            }

            if (command[1].equalsIgnoreCase("fix")) {
                if (fixDoor(player)) {
                    plugin.messageUtils.localizedMessage(player, null, "msg-error-fix");
                }
                return;
            }
        } else if (command.length == 3) {
            if (command[1].equalsIgnoreCase("debug")) {
                plugin.DEBUG = Boolean.parseBoolean(command[2]);
                player.sendMessage(ChatColor.RED + "[Lockette] DEBUG mode is set to " + plugin.DEBUG);
                return;
            }
        }

        // Edit sign text.
        if ((command.length == 2) || (command.length == 3)) {
            if (command[1].equals("1") || command[1].equals("2") || command[1].equals("3") || command[1].equals("4")) {
                Block block = plugin.playerList.get(player.getName());
				//boolean		error = false;

                // Check if the selected block is a valid sign.
                if (block == null) {
                    plugin.messageUtils.localizedMessage(player, null, "msg-error-edit");
                    return;
                } else if (block.getTypeId() != Material.WALL_SIGN.getId()) {
                    plugin.messageUtils.localizedMessage(player, null, "msg-error-edit");
                    return;
                }

                Sign sign = (Sign) block.getState();
                Sign owner = sign;
                String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();
                boolean privateSign;

                // Check if it is our sign that is selected.
                if (text.equals("[private]") || text.equalsIgnoreCase(plugin.altPrivate)) {
                    privateSign = true;
                } else if (text.equals("[more users]") || text.equalsIgnoreCase(plugin.altMoreUsers)) {
                    privateSign = false;

                    Block checkBlock = Lockette.getSignAttachedBlock(block);
                    if (checkBlock == null) {
                        plugin.messageUtils.localizedMessage(player, null, "msg-error-edit");
                        return;
                    }

                    Block signBlock = plugin.findBlockOwner(checkBlock);
                    if (signBlock == null) {
                        plugin.messageUtils.localizedMessage(player, null, "msg-error-edit");
                        return;
                    }

                    owner = (Sign) signBlock.getState();
                } else {
                    plugin.messageUtils.localizedMessage(player, null, "msg-error-edit");
                    return;
                }

                if (plugin.isOwner(owner, player) || plugin.debugMode) {
                    int line = Integer.parseInt(command[1]) - 1;

                    // Disallow editing [Private] line 1 here.
                    if (!plugin.debugMode) {
                        if (line <= 0) {
                            return;
                        }
                        if (line <= 1) {
                            // the onwer is allowed to change his/her name with color as long as color tag is true
                            if (privateSign
                                    && (command.length < 3 || command[2].isEmpty() || !(command[2].replaceAll("&([0-9A-Fa-f])", "").equalsIgnoreCase(player.getName())))) {
                                return;
                            }

                        }
                    }

                    if (command.length == 3) {
                        String id = (!plugin.colorTags) ? command[2].replaceAll("&([0-9A-Fa-f])", "") : command[2];
                        // the owner is allowed to change the 1st line
                        if (plugin.DEBUG) {
                            Lockette.log.log(Level.INFO, "[Lockette] striped name = {0}", command[2].replaceAll("&([0-9A-Fa-f])", ""));
                            Lockette.log.log(Level.INFO, "[Lockette] player name = {0}", player.getName());
                        }
                        //sign.setLine(line, id);
                        plugin.signUtil.setLine(sign, line, id);
                    } else //sign.setLine(line, "");
                    {
                        plugin.signUtil.setLine(sign, line, "");
                    }
                    sign.update();

                    plugin.messageUtils.localizedMessage(player, null, "msg-owner-edit");
                    return;
                } else {
                    plugin.messageUtils.localizedMessage(player, null, "msg-error-edit");
                    return;
                }
            }
        }

        // If none of the above, print out the help text.
        // Commands:
        // reload
        // 2-4 <text> - sign editing
        // link - linking?
        // set <value> <string> - config?
        sendTheLocalizedMessages(plugin, player);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }

        Action action = event.getAction();
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        int type = block.getTypeId();
        BlockFace face = event.getBlockFace();
        ItemStack item;

        if (action == Action.RIGHT_CLICK_BLOCK) {

            // Check we are allowed to used this trapdoor
            if ((plugin.protectTrapDoors) && (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors))) {

                if (interactDoor(block, player)) {
                    return;
                }

                event.setUseInteractedBlock(Result.DENY);
                event.setUseItemInHand(Result.DENY);
                return;
            }

            // Check we are allowed to used this door
            if ((plugin.protectDoors) && (BlockUtil.isInList(type, BlockUtil.materialListDoors))) {
                if (interactDoor(block, player)) {
                    return;
                }

                event.setUseInteractedBlock(Result.DENY);
                event.setUseItemInHand(Result.DENY);
                return;
            }

            if (type == Material.WALL_SIGN.getId()) {
                interactSign(block, player);
                return;
            }

            if (BlockUtil.isInList(type, BlockUtil.materialListChests)) {
                // Try at making a 1.7->1.8 chest fixer.
                plugin.rotateChestOrientation(block, face);
            }

            if (BlockUtil.isInList(type, BlockUtil.materialListNonDoors)
                    || plugin.isInList(type, plugin.customBlockList)) {

                // Trying something out....
                if (plugin.directPlacement) {
                    if (event.hasItem()) {
                        if ((face != BlockFace.UP) && (face != BlockFace.DOWN)) {
                            item = event.getItem();

                            if (item.getTypeId() == Material.SIGN.getId()) {
                                Block checkBlock = block.getRelative(face);

                                type = checkBlock.getTypeId();

                                if (type == Material.AIR.getId()) {
                                    boolean place = false;

                                    if (plugin.isProtected(block)) {
                                        // Add a users sign only if owner.
                                        if (plugin.isOwner(block, player)) {
                                            place = true;
                                        }
                                    } else {
                                        place = true;
                                    }
                                    //if(Lockette.altPrivate == null){}//if(Lockette.altMoreUsers == null){}
                                    if (place) {
                                        //player.sendMessage(ChatColor.RED + "Lockette: Using a sign on a container");

                                        event.setUseItemInHand(Result.ALLOW); //? seems to work in 568
                                        event.setUseInteractedBlock(Result.DENY);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

                if (interactContainer(block, player)) {
                    return;
                }

                event.setUseInteractedBlock(Result.DENY);
                event.setUseItemInHand(Result.DENY);
                return;
            }

            if (type == Material.DIRT.getId()) {
                if (event.hasItem()) {
                    item = event.getItem();

                    type = item.getTypeId();

                    if ((type == Material.DIAMOND_HOE.getId()) || (type == Material.GOLD_HOE.getId()) || (type == Material.IRON_HOE.getId())
                            || (type == Material.STONE_HOE.getId()) || (type == Material.WOOD_HOE.getId())) {
                        Block checkBlock = block.getRelative(BlockFace.UP);

                        type = checkBlock.getTypeId();

                        if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                            event.setUseInteractedBlock(Result.DENY);
                            return;
                        }

                        if (hasAttachedTrapDoor(block)) {
                            event.setUseInteractedBlock(Result.DENY);
                        }
                    }
                }
            }

        } else if (action == Action.LEFT_CLICK_BLOCK) {
            if (plugin.protectTrapDoors) {
                if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                    if (interactDoor(block, player)) {
                        return;
                    }

                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    return;
                }
            }

            if (plugin.protectDoors) {
                if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                    if (interactDoor(block, player)) {
                        return;
                    }

                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Player left, so forget about them.
        plugin.playerList.remove(player.getName());
    }

    //********************************************************************************************************************
    // Start of interact section
    // Returns true if it should be allowed, false if it should be canceled.
    private static boolean interactDoor(Block block, Player player) {
        Block signBlock = plugin.findBlockOwner(block);

        if (signBlock == null) {
            return (true);
        }

        boolean wooden = BlockUtil.isInList(block.getTypeId(), BlockUtil.materialListWoodenDoors);
        boolean trap = false;

        if (plugin.protectTrapDoors) {
            if (BlockUtil.isInList(block.getTypeId(), BlockUtil.materialListTrapDoors)) {
                trap = true;
            }
        }

        // Someone touched an owned door, lets see if they are allowed.
        boolean allow = false;

        if (canInteract(block, signBlock, player, true)) {
            allow = true;
        }

        /*
         // Fee stuff...
         if(!allow){
         // Check if we can pay a fee to activate.
         int fee = getSignOption(signBlock, "fee", Lockette.altFee, 0);
			
         if(signBlock.equals(plugin.playerList.get(player.getName()))){
         if(fee == 0){
         player.sendMessage("unable to pay fee");
         }
         else{
         player.sendMessage("Fee of " +plugin.economyFormat(fee)+" paid (fake)");
         plugin.playerList.put(player.getName(), block);
         allow = true;
         }
         /
         if(fee != 0){
         Sign		sign = (Sign) signBlock.getState();
         String		text = sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "");
					
         if(plugin.economyTransfer(player.getName(), text, fee)){
         allow = true;
         plugin.playerList.put(player.getName(), block);
         }
         else{}
         }/
         }
         else if(fee != 0){
         player.sendMessage("first touch sign to pay fee.");
         }
         }
         */
        if (allow) {
            List<Block> list = plugin.doorUtils.toggleDoors(block, Lockette.getSignAttachedBlock(signBlock), wooden, trap);

            int delta = plugin.getSignOption(signBlock, "timer", plugin.altTimer, plugin.defaultDoorTimer);

            plugin.doorCloser.add(list, delta != 0, delta);
            return (true);
        }

        // Don't have permission.
        //event.setCancelled(true);
        //if(Lockette.oldListener){
        //	if(wooden) Lockette.toggleSingleDoor(block);
        //}
        // Report only once, unless a different block is clicked.
        if (block.equals(plugin.playerList.get(player.getName()))) {
            return (false);
        }
        plugin.playerList.put(player.getName(), block);
        plugin.messageUtils.localizedMessage(player, null, "msg-user-denied-door");
        return (false);
    }

    private static void interactSign(Block block, Player player) {
        Sign sign = (Sign) block.getState();
        String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

        // Check if it is our sign that was clicked.
        if (text.equals("[private]") || text.equalsIgnoreCase(plugin.altPrivate)) {
        } else if (text.equals("[more users]") || text.equalsIgnoreCase(plugin.altMoreUsers)) {
            Block checkBlock = Lockette.getSignAttachedBlock(block);
            if (checkBlock == null) {
                return;
            }

            Block signBlock = plugin.findBlockOwner(checkBlock);
            if (signBlock == null) {
                return;
            }

            sign = (Sign) signBlock.getState();
        } else {
            return;
        }

        // Check owner.
        //if(sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName()) || Lockette.debugMode){			
        if (plugin.isOwner(sign, player) || plugin.debugMode) {
            if (!block.equals(plugin.playerList.get(player.getName()))) {
                // Associate the user with the owned sign.
                plugin.playerList.put(player.getName(), block);
                plugin.messageUtils.localizedMessage(player, null, "msg-help-select");
            }
        } else {/*
             int fee = getSignOption(signBlock, "fee", Lockette.altFee, 0);
			
             if(fee != 0){
             if(!signBlock.equals(plugin.playerList.get(player.getName()))){
             // First half of fee approval.
             plugin.playerList.put(player.getName(), signBlock);
             plugin.localizedMessage(player, null, "msg-user-touch-fee", sign.getLine(1), plugin.economyFormat(fee));
             }
             }
             else{*/

            if (!block.equals(plugin.playerList.get(player.getName()))) {
                // Only print this message once as well.
                plugin.playerList.put(player.getName(), block);
                plugin.messageUtils.localizedMessage(player, null, "msg-user-touch-owned", sign.getLine(1));
            }
            //}
        }
    }

    // Returns true if it should be allowed, false if it should be canceled.
    private static boolean interactContainer(Block block, Player player) {
        Block signBlock = plugin.findBlockOwner(block);

        if (signBlock == null) {
            return (true);
        }

        // Someone touched an owned container, lets see if they are allowed.
        if (canInteract(block, signBlock, player, false)) {
            return (true);
        }

        // Don't have permission.
        // Report only once, unless a different block is clicked.
        if (block.equals(plugin.playerList.get(player.getName()))) {
            return (false);
        }
        plugin.playerList.put(player.getName(), block);
        plugin.messageUtils.localizedMessage(player, null, "msg-user-denied");
        return (false);
    }

    // Block is the container or door, signBlock is the owning [Private] sign.
    // Returns true if it should be allowed, false if it should be canceled.
    private static boolean canInteract(Block block, Block signBlock, Player player, boolean isDoor) {
		// Check if the block is owned first.

        // Moved to outer..
        // Lets see if the player is allowed to touch...
        Sign sign = (Sign) signBlock.getState();
        String line;

        // Check owner and other users
        if (plugin.isUser(block, player, true)) {
            return true;
        }

        // Check admin list last.
        boolean snoop = false;

        if (isDoor) {
            if (plugin.adminBypass) {
                if (plugin.hasPermission(block.getWorld(), player, "lockette.admin.bypass")) {
                    snoop = true;
                }

                if (snoop) {
                    Lockette.log.log(Level.INFO, "[{0}] (Admin) {1} has bypassed a door owned by {2}", new Object[]{plugin.getDescription().getName(), player.getName(), sign.getLine(1)});

                    plugin.messageUtils.localizedMessage(player, null, "msg-admin-bypass", sign.getLine(1));
                    return (true);
                }
            }
        } else if (plugin.adminSnoop) {
            if (plugin.hasPermission(block.getWorld(), player, "lockette.admin.snoop")) {
                snoop = true;
            }

            if (snoop) {
                Lockette.log.log(Level.INFO, "[{0}] (Admin) {1} has snooped around in a container owned by {2}!", new Object[]{plugin.getDescription().getName(), player.getName(), sign.getLine(1)});

                plugin.messageUtils.localizedMessage(player, plugin.broadcastSnoopTarget, "msg-admin-snoop", sign.getLine(1));
                return (true);
            }
        }

        // Don't have permission.
        return (false);
    }

    //********************************************************************************************************************
    // Start of utility section
    // Returns true if a door wasn't changed.
    private static boolean fixDoor(Player player) {
        Block block = player.getTargetBlock((HashSet<Byte>) null, 10);
        int type = block.getTypeId();
        boolean doCheck = false;

        // Check if the block being looked at is a door block.
        if (plugin.protectTrapDoors) {
            if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                doCheck = true;
            }
        }

        if (plugin.protectDoors) {
            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                doCheck = true;
            }
        }

        if (!doCheck) {
            return (true);
        }

        Block signBlock = plugin.findBlockOwner(block);

        if (signBlock == null) {
            return (true);
        }

        Sign sign = (Sign) signBlock.getState();

        // Check owner only.
        if (plugin.isOwner(block, player)) {
            plugin.doorUtils.toggleSingleDoor(block);
            return (false);
        }

        return (true);
    }

    public static boolean hasAttachedTrapDoor(Block block) {
        Block checkBlock;
        int type;
        int face;

        checkBlock = block.getRelative(BlockFace.NORTH);
        type = checkBlock.getTypeId();
        if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
            face = checkBlock.getData() & 0x3;
            if (face == 2) {
                return (true);
            }
        }

        checkBlock = block.getRelative(BlockFace.EAST);
        type = checkBlock.getTypeId();
        if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
            face = checkBlock.getData() & 0x3;
            if (face == 0) {
                return (true);
            }
        }

        checkBlock = block.getRelative(BlockFace.SOUTH);
        type = checkBlock.getTypeId();
        if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
            face = checkBlock.getData() & 0x3;
            if (face == 3) {
                return (true);
            }
        }

        checkBlock = block.getRelative(BlockFace.WEST);
        type = checkBlock.getTypeId();
        if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
            face = checkBlock.getData() & 0x3;
            if (face == 1) {
                return (true);
            }
        }

        return (false);
    }

    private void sendTheLocalizedMessages(Lockette plugin, Player player) {
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command1");
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command2");
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command3");
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command4");
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command5");
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command6");
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command7");
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command8");
        plugin.messageUtils.localizedMessage(player, null, "msg-help-command9");
    }
}
