//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//
package org.yi.acru.bukkit.Lockette;

// Imports.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.yi.acru.bukkit.PluginCore;
import org.yi.acru.bukkit.BlockUtil;

import org.bukkit.metadata.*;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.json.simple.*;
import org.json.simple.parser.*;
import org.yi.acru.bukkit.Lockette.Utils.DoorUtils;
import org.yi.acru.bukkit.Lockette.Utils.NameLookup;
import org.yi.acru.bukkit.Lockette.Utils.SignUtil;

public class Lockette extends PluginCore {

    boolean DEBUG = false;

    private Lockette plugin;
    private boolean enabled = false;
    
    private String logName;
    private String version;

    private MutableBoolean uuidSupport = new MutableBoolean(false);
    private boolean registered = false;
    private final LocketteBlockListener blockListener = new LocketteBlockListener(this);
    private final LocketteEntityListener entityListener = new LocketteEntityListener(this);
    private final LockettePlayerListener playerListener = new LockettePlayerListener(this);
    private final LockettePrefixListener prefixListener = new LockettePrefixListener(this);
    private final LocketteWorldListener worldListener = new LocketteWorldListener(this);
    private final LocketteInventoryListener inventoryListener = new LocketteInventoryListener(this);
    protected final LocketteDoorCloser doorCloser = new LocketteDoorCloser(this);

    protected  boolean explosionProtectionAll, rotateChests;
    protected boolean adminSnoop, adminBypass, adminBreak;
    protected boolean protectDoors, protectTrapDoors, usePermissions;
    protected boolean directPlacement, colorTags, debugMode;
    protected boolean blockHopper = false;
    protected int defaultDoorTimer;
    protected String broadcastSnoopTarget, broadcastBreakTarget, broadcastReloadTarget;

    protected boolean msgUser, msgOwner, msgAdmin, msgError, msgHelp;
    protected String altPrivate, altMoreUsers, altEveryone, altOperators, altTimer, altFee;
    protected List<Object> customBlockList = null, disabledPluginList = null;

    protected FileConfiguration strings = null;
    protected final HashMap<String, Block> playerList = new HashMap<String, Block>();

    /*private*/ static final String META_KEY = "LocketteUUIDs";
    private LocketteProperties properties;
    
    public SignUtil signUtil;
    public DoorUtils doorUtils;
    
    public Lockette() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        if (enabled) {
            return;
        }
        setStuff();
        signUtil = new SignUtil(this);
        doorUtils = new DoorUtils();

        log.info(logName + " Version " + version + " is being enabled!  Yay!  (Core version " + getCoreVersion() + ")");

        // Check build version.
        final int recBuild = 2771;
        final int minBuild = 2735;
        int printBuild;
        float build = getBuildVersion();

        if ((build > 399) && (build < 400)) {
            printBuild = (int) ((build - 399) * 100);
        } else {
            printBuild = (int) build;
        }

        //if((printBuild >= 45) && (printBuild <= 49)) log.info("[" + getDescription().getName() + "] Ignore the warning about using the stupidly long constructor!");
		/*
         if(build == 0){
         log.warning("[" + getDescription().getName() + "] Craftbukkit build unrecognized, please be sure you have build [" + recBuild + "] or greater.");
         }
         else if(build < minBuild){
         log.severe("[" + getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "], but requires requires build [" + minBuild + "] or greater!");
         log.severe("[" + getDescription().getName() + "] Aborting enable!");
         return;
         }
         else if(build < recBuild){
         log.warning("[" + getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "], but the recommended build is [" + recBuild + "] or greater.");
         }
         else if((build >= 605) && (build <= 612)){
         log.warning("[" + getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "], but this build is buggy!  Please upgrade to build 617 or greater.");
         }
         else if((build >= 685) && (build <= 703)){
         log.warning("[" + getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "], but this build is buggy!  Please upgrade to build 704 or greater.");
         }
         else{
         log.info("[" + getDescription().getName() + "] Detected craftbukkit build [" + printBuild + "] ok.");
         }
         */
        String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
        float bukkitver = Float.parseFloat(bukkitVersion.substring(1, 4).replace("_", "."));
        float bukkitminver = 1.8F;

        if (bukkitver < bukkitminver) {
            log.severe(logName + " Detected Bukkit build [" + bukkitVersion + "], but requires version [" + bukkitminver + "] or greater!");
            log.severe(logName + " Aborting enable!");
            return;
        } else {
            log.info(logName + " Detected Bukkit version [" + bukkitVersion + "] ok.");
        }

        // Load properties and strings.
        properties = new LocketteProperties(plugin);
        properties.loadProperties(false);

        // Load external permission/group plugins.
        super.onEnable();

        // Reg us some events yo!	
        if (!registered) {
            blockListener.registerEvents();
            entityListener.registerEvents();
            playerListener.registerEvents();
            prefixListener.registerEvents();
            worldListener.registerEvents();
            inventoryListener.registerEvents();
            registered = true;
        }

        // All done.
        log.info(logName + " Ready to protect your containers.");
        enabled = true;
    }

    @Override
    public void onDisable() {
        if (!enabled) {
            return;
        }
        log.info(this.getDescription().getName() + " is being disabled...  ;.;");

        if (protectDoors || protectTrapDoors) {
            log.info(logName + " Closing all automatic doors.");
            doorCloser.cleanup();
        }

        super.onDisable();

        enabled = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("lockette")) {
            return (false);
        }
        if (sender instanceof Player) {
            return (true);	// Handling in command preprocess for now.
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                properties.loadProperties(true);

                localizedMessage(null, broadcastReloadTarget, "msg-admin-reload");

                //String msgString = Lockette.strings.getString("msg-admin-reload");
                //selectiveBroadcast(Lockette.broadcastReloadTarget, ChatColor.RED + "Lockette: " + msgString);
            } else if (args[0].equalsIgnoreCase("coredump")) {
                dumpCoreInfo();
            }
        }
        //sender.sendMessage("Lockette: Test");

        return (true);
    }

    //********************************************************************************************************************
    // Start of public section
    public boolean isProtected(Block block) {
        if (!enabled) {
            return (false);
        }

        int type = block.getTypeId();

        if (type == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign) block.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

            if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                return (true);
            } else if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
                Block checkBlock = getSignAttachedBlock(block);

                if (checkBlock != null) {
                    if (findBlockOwner(checkBlock) != null) {
                        return (true);
                    }
                }
            }
        } else if (findBlockOwner(block) != null) {
            log.info("--- FindBlockOwner(" + block + ") != null.  Returning TRUE. Lockette thinks something is protecting this.");
            return (true);
        }

        return (false);
    }

    public String getProtectedOwner(Block block) {
        return Bukkit.getOfflinePlayer(getProtectedOwnerUUID(block)).getName();
    }

    public UUID getProtectedOwnerUUID(Block block) {
        if (!enabled) {
            return (null);
        }

        int type = block.getTypeId();

        if (type == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign) block.getState();
            String text = ChatColor.stripColor(sign.getLine(0)).toLowerCase();

            if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                return signUtil.getUUIDFromMeta(sign, 1);
            } else if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
                Block checkBlock = getSignAttachedBlock(block);

                if (checkBlock != null) {
                    Block signBlock = findBlockOwner(checkBlock);

                    if (signBlock != null) {
                        sign = (Sign) signBlock.getState();
                        return signUtil.getUUIDFromMeta(sign, 1);
                    }
                }
            }
        } else {
            Block signBlock = findBlockOwner(block);
            if (signBlock != null) {
                Sign sign = (Sign) signBlock.getState();
                return signUtil.getUUIDFromMeta(sign, 1);
            }
        }

        return null;
    }

    public boolean isEveryone(Block block) {
        if (!enabled) {
            return (true);
        }

        Block signBlock = findBlockOwner(block);

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

                if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(altEveryone)) {
                    return (true);
                }
            }
        }

        // Check for more users.
        List<Block> list = findBlockUsers(block, signBlock);
        int x, count = list.size();

        for (x = 0; x < count; ++x) {
            sign = (Sign) list.get(x).getState();

            for (y = 1; y <= 3; ++y) {
                if (!sign.getLine(y).isEmpty()) {
                    line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

                    if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(altEveryone)) {
                        return (true);
                    }
                }
            }
        }

        // Everyone doesn't have permission.
        return (false);
    }

    //********************************************************************************************************************
    // Start of external permissions section
    @Override
    protected boolean pluginEnableOverride(String pluginName) {
        return (isInList(pluginName, disabledPluginList));
    }

    @Override
    protected boolean usingExternalPermissions() {
        if (!usePermissions) {
            return (false);
        }
        return (super.usingExternalPermissions());
        //return(usePermissions);
    }

    @Override
    protected boolean usingExternalZones() {
        return (super.usingExternalZones());
    }

    @Override
    protected String getLocalizedEveryone() {
        return (altEveryone);
    }

    @Override
    protected String getLocalizedOperators() {
        return (altOperators);
    }

    //********************************************************************************************************************
    // Start of utility section
    protected void localizedMessage(Player player, String broadcast, String key) {
        localizedMessage(player, broadcast, key, null, null);
    }

    protected void localizedMessage(Player player, String broadcast, String key, String sub) {
        localizedMessage(player, broadcast, key, sub, null);
    }

    protected void localizedMessage(Player player, String broadcast, String key, String sub, String num) {
        String color = "";

        // Filter and color based on message type.
        if (key.startsWith("msg-user-")) {
            if (broadcast == null) {
                if (!msgUser) {
                    return;
                }
            }
            color = ChatColor.YELLOW.toString();
        } else if (key.startsWith("msg-owner-")) {
            if (broadcast == null) {
                if (!msgOwner) {
                    return;
                }
            }
            color = ChatColor.GOLD.toString();
        } else if (key.startsWith("msg-admin-")) {
            if (broadcast == null) {
                if (!msgAdmin) {
                    return;
                }
            }
            color = ChatColor.RED.toString();
        } else if (key.startsWith("msg-error-")) {
            if (broadcast == null) {
                if (!msgError) {
                    return;
                }
            }
            color = ChatColor.RED.toString();
        } else if (key.startsWith("msg-help-")) {
            if (broadcast == null) {
                if (!msgHelp) {
                    return;
                }
            }
            color = ChatColor.GOLD.toString();
        }

        // Fetch the requested message string.
        String message = strings.getString(key);
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
            selectiveBroadcast(broadcast, color + "[Lockette] " + message);
        } else if (player != null) {
            player.sendMessage(color + "[Lockette] " + message);
        }
    }

    // Version for determining if a container is released.
    // Should return non-null if destroying the block will surely cause the the sign to fall off.
    // Okay for trap doors, though could be optimized.
    protected Block findBlockOwnerBreak(Block block) {
        int type = block.getTypeId();

        // Check known block types.
        if (BlockUtil.isInList(type, BlockUtil.materialListChests)) {
            return (findBlockOwnerBase(block, null, false, false, false, false, false));
        }
        if (BlockUtil.isInList(type, BlockUtil.materialListTools) || plugin.isInList(type, plugin.customBlockList)) {
            return (findBlockOwnerBase(block, null, false, false, false, false, false));
        }
        if (plugin.protectTrapDoors) {
            if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                return (findBlockOwnerBase(block, null, false, false, false, false, false));
            }
        }
        if (plugin.protectDoors) {
            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                return (findBlockOwnerBase(block, null, false, true, true, false, false));
            }
        }

        Block checkBlock;

        // This should be edited if invalid signs can be destroyed..........
        checkBlock = findBlockOwnerBase(block, null, false, false, false, false, false);
        if (checkBlock != null) {
            return (checkBlock);
        }

        if (protectTrapDoors) {
            // Need to check if there is a trap door attached to the block, and check for a sign attached there.
            // This is the bit that could be optimized.

            checkBlock = block.getRelative(BlockFace.NORTH);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListTrapDoors)) {
                if ((checkBlock.getData() & 0x3) == 2) {
                    checkBlock = findBlockOwnerBase(checkBlock, null, false, false, false, false, false);
                    if (checkBlock != null) {
                        return (checkBlock);
                    }
                }
            }

            checkBlock = block.getRelative(BlockFace.EAST);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListTrapDoors)) {
                if ((checkBlock.getData() & 0x3) == 0) {
                    checkBlock = findBlockOwnerBase(checkBlock, null, false, false, false, false, false);
                    if (checkBlock != null) {
                        return (checkBlock);
                    }
                }
            }

            checkBlock = block.getRelative(BlockFace.SOUTH);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListTrapDoors)) {
                if ((checkBlock.getData() & 0x3) == 3) {
                    checkBlock = findBlockOwnerBase(checkBlock, null, false, false, false, false, false);
                    if (checkBlock != null) {
                        return (checkBlock);
                    }
                }
            }

            checkBlock = block.getRelative(BlockFace.WEST);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListTrapDoors)) {
                if ((checkBlock.getData() & 0x3) == 1) {
                    checkBlock = findBlockOwnerBase(checkBlock, null, false, false, false, false, false);
                    if (checkBlock != null) {
                        return (checkBlock);
                    }
                }
            }
        }

        if (protectDoors) {
            // Need to check if there is a door above block, and check for a sign attached there.

            checkBlock = block.getRelative(BlockFace.UP);
            type = checkBlock.getTypeId();

            if (!BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                // Handle door above type.

                return (findBlockOwnerBase(checkBlock, null, false, true, true, false, false));
            }
        }

        return (null);
    }

    // Find the owner for any 'block'.
    protected Block findBlockOwner(Block block) {
        // Pass to a special version with specific values.
        // Moved this next to the same overloaded function where it belongs.
        return (findBlockOwner(block, null, false));
    }

    // Version for finding conflicts, when creating a new sign.
    // Ignore the sign being made, in case another plugin has set the text of the sign prematurely.
    protected Block findBlockOwner(Block block, Block ignoreBlock, boolean iterateFurther) {
        plugin.log.info("FindBlockOwner( " + block + " " + ignoreBlock + " " + iterateFurther + " )");

        if (block == null) {
            return null;
        }

        int type = block.getTypeId();
        Location ignore = null;

        if (ignoreBlock != null) {
            ignore = ignoreBlock.getLocation();
        }

        // Check known block types.
        if (BlockUtil.isInList(type, BlockUtil.materialListChests)) {
            return (findBlockOwnerBase(block, ignore, true, false, false, false, false));
        }
        if (BlockUtil.isInList(type, BlockUtil.materialListTools) || plugin.isInList(type, plugin.customBlockList)) {
            return (findBlockOwnerBase(block, ignore, false, false, false, false, false));
        }
        if (protectTrapDoors) {
            if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                // Need to check block it is attached to as well as other attached trap doors.
                //return(findBlockOwnerBase(block, ignore, false, false, false, false, false));				
                return (findBlockOwner(getTrapDoorAttachedBlock(block), ignoreBlock, false));
            }
        }
        if (protectDoors) {
            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                return (findBlockOwnerBase(block, ignore, true, true, true, true, iterateFurther));
            }
        }

        Block checkBlock, result;

        if (protectTrapDoors) {
            // Check base block, as it might have the sign and it isn't checked below.

            checkBlock = findBlockOwnerBase(block, ignore, false, false, false, false, false);
            if (checkBlock != null) {
                return (checkBlock);
            }

            // Need to check if there is a trap door attached to the block, and check for a sign attached there.
            checkBlock = block.getRelative(BlockFace.NORTH);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListTrapDoors)) {
                if ((checkBlock.getData() & 0x3) == 2) {
                    checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
                    if (checkBlock != null) {
                        return (checkBlock);
                    }
                }
            }

            checkBlock = block.getRelative(BlockFace.EAST);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListTrapDoors)) {
                if ((checkBlock.getData() & 0x3) == 0) {
                    checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
                    if (checkBlock != null) {
                        return (checkBlock);
                    }
                }
            }

            checkBlock = block.getRelative(BlockFace.SOUTH);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListTrapDoors)) {
                if ((checkBlock.getData() & 0x3) == 3) {
                    checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
                    if (checkBlock != null) {
                        return (checkBlock);
                    }
                }
            }

            checkBlock = block.getRelative(BlockFace.WEST);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListTrapDoors)) {
                if ((checkBlock.getData() & 0x3) == 1) {
                    checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
                    if (checkBlock != null) {
                        return (checkBlock);
                    }
                }
            }
        }

        if (protectDoors) {
            // Don't check the block but check for doors above then below the block, which includes the block.

            checkBlock = block.getRelative(BlockFace.UP);
            type = checkBlock.getTypeId();
            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                // Handle door above type.

                result = findBlockOwnerBase(checkBlock, ignore, true, true, true, true, iterateFurther);
                if (result != null) {
                    return (result);
                }
            }

            // This is needed to protect the other block above double doors.
            checkBlock = block.getRelative(BlockFace.DOWN);
            type = checkBlock.getTypeId();
            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                // For door below only.
                // Don't include the block below door, as a sign there would not protect the target block.

                Block checkBlock2 = checkBlock.getRelative(BlockFace.DOWN);
                type = checkBlock2.getTypeId();
                if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                    return (findBlockOwnerBase(checkBlock2, ignore, true, true, false, true, iterateFurther));
                } else {
                    return (findBlockOwnerBase(checkBlock, ignore, true, true, false, true, iterateFurther));
                }
            }
        }

        return (null);
    }

    // Should only be called by the above related functions.
    // Should generally not be passed a hinge block, only a known container or door.
    private Block findBlockOwnerBase(Block block, Location ignore, boolean iterate, boolean iterateUp, boolean iterateDown, boolean includeEnds, boolean iterateFurther) {
        Block checkBlock;
        int type;
        byte face;
        boolean doCheck;

        // Check up and down along door surfaces, with a recursive call and iterate false.
        if (iterateUp) {
            checkBlock = block.getRelative(BlockFace.UP);
            type = checkBlock.getTypeId();

            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                checkBlock = findBlockOwnerBase(checkBlock, ignore, false, iterateUp, false, includeEnds, false);
            } else if (includeEnds) {
                checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, includeEnds, false);
            } else {
                checkBlock = null;
            }

            if (checkBlock != null) {
                return (checkBlock);
            }
        }

        if (iterateDown) {
            checkBlock = block.getRelative(BlockFace.DOWN);
            type = checkBlock.getTypeId();

            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, iterateDown, includeEnds, false);
            } else if (includeEnds) {
                checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, includeEnds, false);
            } else {
                checkBlock = null;
            }

            if (checkBlock != null) {
                return (checkBlock);
            }
        }

        // Check around the originating block, in the order NESW.
        // If a sign is found and it is not the ignored block, check the text.
        // If it is not a sign and iterate is true, do a recursive call with iterate false.
        // (Or further, though this currently backtracks slightly.)
        checkBlock = block.getRelative(BlockFace.NORTH);
        if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
            face = checkBlock.getData();
            if (face == BlockUtil.faceList[2]) {
                // Ignore a sign being created.

                if (ignore == null) {
                    doCheck = true;
                } else doCheck = !checkBlock.getLocation().equals(ignore);

                if (doCheck) {
                    Sign sign = (Sign) checkBlock.getState();
                    String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

                    if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                        return (checkBlock);
                    }
                }
            }
        } else if (iterate) {
            if (checkBlock.getTypeId() == block.getTypeId()) {
                checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
                if (checkBlock != null) {
                    return (checkBlock);
                }
            }
        }

        checkBlock = block.getRelative(BlockFace.EAST);
        if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
            face = checkBlock.getData();
            if (face == BlockUtil.faceList[3]) {
                // Ignore a sign being created.

                if (ignore == null) {
                    doCheck = true;
                } else doCheck = !checkBlock.getLocation().equals(ignore);

                if (doCheck) {
                    Sign sign = (Sign) checkBlock.getState();
                    String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

                    if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                        return (checkBlock);
                    }
                }
            }
        } else if (iterate) {
            if (checkBlock.getTypeId() == block.getTypeId()) {
                checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
                if (checkBlock != null) {
                    return (checkBlock);
                }
            }
        }

        checkBlock = block.getRelative(BlockFace.SOUTH);
        if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
            face = checkBlock.getData();
            if (face == BlockUtil.faceList[0]) {
                // Ignore a sign being created.

                if (ignore == null) {
                    doCheck = true;
                } else doCheck = !checkBlock.getLocation().equals(ignore);

                if (doCheck) {
                    Sign sign = (Sign) checkBlock.getState();
                    String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

                    if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                        return (checkBlock);
                    }
                }
            }
        } else if (iterate) {
            if (checkBlock.getTypeId() == block.getTypeId()) {
                checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
                if (checkBlock != null) {
                    return (checkBlock);
                }
            }
        }

        checkBlock = block.getRelative(BlockFace.WEST);
        if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
            face = checkBlock.getData();
            if (face == BlockUtil.faceList[1]) {
                // Ignore a sign being created.

                if (ignore == null) {
                    doCheck = true;
                } else doCheck = !checkBlock.getLocation().equals(ignore);

                if (doCheck) {
                    Sign sign = (Sign) checkBlock.getState();
                    String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

                    if (text.equals("[private]") || text.equalsIgnoreCase(altPrivate)) {
                        return (checkBlock);
                    }
                }
            }
        } else if (iterate) {
            if (checkBlock.getTypeId() == block.getTypeId()) {
                checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
                if (checkBlock != null) {
                    return (checkBlock);
                }
            }
        }

        return (null);
    }

    protected List<Block> findBlockUsers(Block block, Block signBlock) {
        int type = block.getTypeId();

        if (BlockUtil.isInList(type, BlockUtil.materialListChests)) {
            return (findBlockUsersBase(block, true, false, false, false, 0));
        }
        if (protectTrapDoors) {
            if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                return (findBlockUsersBase(getTrapDoorAttachedBlock(block), false, false, false, true, 0));
            }
        }
        if (protectDoors) {
            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                return (findBlockUsersBase(block, true, true, true, false, signBlock.getY()));
            }
        }
        return (findBlockUsersBase(block, false, false, false, false, 0));
    }

    private List<Block> findBlockUsersBase(Block block, boolean iterate, boolean iterateUp, boolean iterateDown, boolean traps, int includeYPos) {
        Block checkBlock;
        int type;
        byte face;
        List<Block> list = new ArrayList<>();

        // Experimental door code, check up and down.
        if (iterateUp) {
            checkBlock = block.getRelative(BlockFace.UP);
            type = checkBlock.getTypeId();

            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, false, false, includeYPos));
            } // Limitation for more users sign.
            else if (checkBlock.getY() == includeYPos) {
                list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
            }
        }

        if (iterateDown) {
            checkBlock = block.getRelative(BlockFace.DOWN);
            type = checkBlock.getTypeId();

            if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
                list.addAll(findBlockUsersBase(checkBlock, false, false, iterateDown, false, includeYPos));
            } // No limitation here.
            else {
                list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
            }
        }

        // Check around the originating block, in the order NESW.
        checkBlock = block.getRelative(BlockFace.NORTH);
        type = checkBlock.getTypeId();
        if (type == Material.WALL_SIGN.getId()) {
            face = checkBlock.getData();
            if (face == BlockUtil.faceList[2]) {
                Sign sign = (Sign) checkBlock.getState();
                String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

                if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
                    list.add(checkBlock);
                }
            }
        } else if (iterate) {
            if (type == block.getTypeId()) {
                list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
            }
        } else if (traps) {
            if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                face = checkBlock.getData();
                if ((face & 3) == 2) {
                    list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
                }
            }
        }

        checkBlock = block.getRelative(BlockFace.EAST);
        type = checkBlock.getTypeId();
        if (type == Material.WALL_SIGN.getId()) {
            face = checkBlock.getData();
            if (face == BlockUtil.faceList[3]) {
                Sign sign = (Sign) checkBlock.getState();
                String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

                if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
                    list.add(checkBlock);
                }
            }
        } else if (iterate) {
            if (type == block.getTypeId()) {
                list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
            }
        } else if (traps) {
            if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                face = checkBlock.getData();
                if ((face & 3) == 0) {
                    list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
                }
            }
        }

        checkBlock = block.getRelative(BlockFace.SOUTH);
        type = checkBlock.getTypeId();
        if (type == Material.WALL_SIGN.getId()) {
            face = checkBlock.getData();
            if (face == BlockUtil.faceList[0]) {
                Sign sign = (Sign) checkBlock.getState();
                String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

                if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
                    list.add(checkBlock);
                }
            }
        } else if (iterate) {
            if (type == block.getTypeId()) {
                list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
            }
        } else if (traps) {
            if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                face = checkBlock.getData();
                if ((face & 3) == 3) {
                    list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
                }
            }
        }

        checkBlock = block.getRelative(BlockFace.WEST);
        type = checkBlock.getTypeId();
        if (type == Material.WALL_SIGN.getId()) {
            face = checkBlock.getData();
            if (face == BlockUtil.faceList[1]) {
                Sign sign = (Sign) checkBlock.getState();
                String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

                if (text.equals("[more users]") || text.equalsIgnoreCase(altMoreUsers)) {
                    list.add(checkBlock);
                }
            }
        } else if (iterate) {
            if (type == block.getTypeId()) {
                list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
            }
        } else if (traps) {
            if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)) {
                face = checkBlock.getData();
                if ((face & 3) == 1) {
                    list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
                }
            }
        }

        return (list);
    }

    protected int findChestCountNear(Block block) {
        return (findChestCountNearBase(block, (byte) 0));
    }

    private int findChestCountNearBase(Block block, byte face) {
        int count = 0;
        Block checkBlock;

        if (face != 2) {
            checkBlock = block.getRelative(BlockFace.NORTH);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListChests) && (checkBlock.getTypeId() == block.getTypeId())) {
                ++count;
                if (face == 0) {
                    count += findChestCountNearBase(checkBlock, (byte) 3);
                }
            }
        }

        if (face != 5) {
            checkBlock = block.getRelative(BlockFace.EAST);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListChests) && (checkBlock.getTypeId() == block.getTypeId())) {
                ++count;
                if (face == 0) {
                    count += findChestCountNearBase(checkBlock, (byte) 4);
                }
            }
        }

        if (face != 3) {
            checkBlock = block.getRelative(BlockFace.SOUTH);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListChests) && (checkBlock.getTypeId() == block.getTypeId())) {
                ++count;
                if (face == 0) {
                    count += findChestCountNearBase(checkBlock, (byte) 2);
                }
            }
        }

        if (face != 4) {
            checkBlock = block.getRelative(BlockFace.WEST);
            if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListChests) && (checkBlock.getTypeId() == block.getTypeId())) {
                ++count;
                if (face == 0) {
                    count += findChestCountNearBase(checkBlock, (byte) 5);
                }
            }
        }

        return (count);
    }

    protected void rotateChestOrientation(Block block, BlockFace blockFace) {

        if (!BlockUtil.isInList(block.getTypeId(), BlockUtil.materialListChests)) {
            return;
        }
        if (!rotateChests) {
            if (block.getData() != 0) {
                return;
            }
        }

        byte face;

        if (blockFace == BlockFace.NORTH) {
            face = BlockUtil.faceList[2];
        } else if (blockFace == BlockFace.EAST) {
            face = BlockUtil.faceList[3];
        } else if (blockFace == BlockFace.SOUTH) {
            face = BlockUtil.faceList[0];
        } else if (blockFace == BlockFace.WEST) {
            face = BlockUtil.faceList[1];
        } else {
            return;
        }

        Block checkBlock;

        checkBlock = block.getRelative(BlockFace.NORTH);
        if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListChests) && (checkBlock.getTypeId() == block.getTypeId())) {
            if ((face == BlockUtil.faceList[1]) || (face == BlockUtil.faceList[3])) {
                block.setData(face);
                checkBlock.setData(face);
            }
            return;
        }

        checkBlock = block.getRelative(BlockFace.EAST);
        if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListChests) && (checkBlock.getTypeId() == block.getTypeId())) {
            if ((face == BlockUtil.faceList[2]) || (face == BlockUtil.faceList[0])) {
                block.setData(face);
                checkBlock.setData(face);
            }
            return;
        }

        checkBlock = block.getRelative(BlockFace.SOUTH);
        if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListChests) && (checkBlock.getTypeId() == block.getTypeId())) {
            if ((face == BlockUtil.faceList[1]) || (face == BlockUtil.faceList[3])) {
                block.setData(face);
                checkBlock.setData(face);
            }
            return;
        }

        checkBlock = block.getRelative(BlockFace.WEST);
        if (BlockUtil.isInList(checkBlock.getTypeId(), BlockUtil.materialListChests) && (checkBlock.getTypeId() == block.getTypeId())) {
            if ((face == BlockUtil.faceList[2]) || (face == BlockUtil.faceList[0])) {
                block.setData(face);
                checkBlock.setData(face);
            }
            return;
        }

        block.setData(face);
    }




    //********************************************************************************************************************
    // Start of utility section
    protected int getSignOption(Block signBlock, String tag, String altTag, int defaultValue) {
        Sign sign = (Sign) signBlock.getState();

        // Check main two users.
        String line;
        int x, y, end, index;

        for (y = 2; y <= 3; ++y) {
            if (!sign.getLine(y).isEmpty()) {
                line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");
                //if(line.isEmpty()) continue;

                end = line.length() - 1;

                if (end >= 2) {
                    if ((line.charAt(0) == '[') && (line.charAt(end) == ']')) {
                        index = line.indexOf(":");

                        if (index == -1) {
                            // No number.
                            if (line.substring(1, end).equalsIgnoreCase(tag) || line.substring(1, end).equalsIgnoreCase(altTag)) {
                                return (defaultValue);
                            }
                        } else {
                            // Number.
                            if (line.substring(1, index).equalsIgnoreCase(tag) || line.substring(1, index).equalsIgnoreCase(altTag)) {
                                // Trim junk around the number.

                                for (x = index; x < end; ++x) {
                                    if (Character.isDigit(line.charAt(x))) {
                                        index = x;
                                        break;
                                    }
                                }
                                for (x = index + 1; x < end; ++x) {
                                    if (!Character.isDigit(line.charAt(x))) {
                                        end = x;
                                        break;
                                    }
                                }

                                // Try to parse the number, and return the result.
                                try {
                                    int value = Integer.parseInt(line.substring(index, end));
                                    return (value);
                                } catch (NumberFormatException ex) {
                                    return (defaultValue);
                                }
                            }
                        }
                    }
                }
            }
        }

        return (defaultValue);
    }

    protected boolean isInList(Object target, List<Object> list) {
        if (list == null) {
            return (false);
        }
        for (int x = 0; x < list.size(); ++x) {
            if (list.get(x).equals(target)) {
                return (true);
            }
        }
        return (false);
    }

    private boolean isHackFormat(String line) {
        String[] strs = line.split(":");
        return (line.indexOf(":") > 1 && strs[1].length() == 36) ? true : false;
    }

    private String trim(String str) {
        return str == null ? null : str.trim();
    }

    // extract palyer name from the playerID string
    private String getPlayerName(String str) {
        return trim(((str.indexOf(":") > 0) ? str.split(":")[0] : str));
    }

    private String getPlayerUUIDString(String str) {
        return trim(((str.indexOf(":") > 0) ? str.split(":")[1] : str));
    }

    private UUID getPlayerUUID(String str) {
        return UUID.fromString(getPlayerUUIDString(str));
    }







    private boolean oldFormatCheck(String signname, String pname) {
        signname = ChatColor.stripColor(signname);
        pname = ChatColor.stripColor(pname);
        int length = pname.length();
        if (length > 15) {
            length = 15;
        }
        return signname.equalsIgnoreCase(pname.substring(0, length));
    }

    private boolean matchUserUUID(Sign sign, int index, OfflinePlayer player, boolean update) {
        try {
            String line = sign.getLine(index);
            String checkline = ChatColor.stripColor(line);

            if ((checkline.indexOf("[") == 0 && checkline.indexOf("]") > 1)
                    || line.isEmpty()) {
                return false;
            }

            // no uuid support? then just compare name against typed
            if (!uuidSupport.booleanValue()) {	// 
                if (DEBUG) {
                    plugin.log.info("[Lockette] NO UUID support, doing old name checking.");
                }
                //return checkline.split(":")[0].trim().equals(player.getName());
                String pname = player.getName();
                String against = checkline.split(":")[0].trim();
                return oldFormatCheck(against, pname);
            }

            UUID uuid = null;
            String name = getPlayerName(line);
            if (DEBUG) {
                plugin.log.info("[Lockette] Name on the sign is : " + name);
            }

            if (isHackFormat(line)) {
                //if it's hacked uuid line, convert to metadata
                // if hacked UUID line, get the UUID
                try {
                    uuid = getPlayerUUID(line);
                } catch (IllegalArgumentException e) {
                    log.info("[" + plugin.getDescription().getName() + "] Invalid Player UUID!");
                    return false;
                }
                if (uuid != null && update) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    if (DEBUG) {
                        plugin.log.info("[Lockette] updating the old hacked format for " + p);
                    }
                    signUtil.setLine(sign, index, name, p);
                }
                // update sign for later uuid check!
                sign.update();
            }

            // not old hack UUID format and just player name?
            // then convert the existing name to uuid then compare uuid
            if (!sign.hasMetadata(META_KEY) || signUtil.getUUIDFromMeta(sign, index) == null) {
                if (DEBUG) {
                    log.info("[Lockette] Checking for original format for " + checkline);
                }
                OfflinePlayer oplayer = Bukkit.getOfflinePlayer(checkline);
                if (oplayer != null && oplayer.hasPlayedBefore()) {
                    if (DEBUG) {
                        log.info("[Lockette] converting original format for " + oplayer + " name = " + checkline);
                    }
                    signUtil.setLine(sign, index, line, oplayer);
                } else {
                    // partial check with long name.
                    String pname = player.getName();
                    String against = checkline.split(":")[0].trim();
                    if (oldFormatCheck(against, pname)) {
                        if (DEBUG) {
                            plugin.log.info("[Lockette] Partial match! Converting original format for " + player.getName() + " name = " + checkline);
                        }
                        signUtil.setLine(sign, index, player.getName(), player);
                    }
                    // if even partial matching is not found, leave it as is.
					/*
                     else {
                     Lockette.log.log(Level.INFO, "[Lockette] Can't convert {0} !", line);
                     setLine(sign, index, line);
                     if (index == 1){
                     sign.setLine(0, "[?]");
                     }
                     }
                     */
                }
                // update sign for later uuid check!
                sign.update();
            }

            uuid = signUtil.getUUIDFromMeta(sign, index);

            if (DEBUG) {
                log.info("[Lockette] uuid on the sign = " + uuid);
                log.info("[Lockette] player's uuid    = " + player.getUniqueId());
            }

            if (uuid != null) {
                if (uuid.equals(player.getUniqueId())) {
                    //Check if the Player name has changen and update it
                    if (!ChatColor.stripColor(ChatColor.stripColor(name)).equals(player.getName())) {
                        sign.setLine(index, player.getName());
                        sign.update();
                    }
                    return true;
                }

                // this to remove falsely generated uuid.
                OfflinePlayer oplayer = Bukkit.getOfflinePlayer(uuid);
                if (!oplayer.hasPlayedBefore()) {
                    if (DEBUG) {
                        log.info("[Lockette] removing bad UUID");
                    }
                    signUtil.removeUUIDMetadata(sign);
                }
            } else { // check the name history
                NameLookup lookup = new NameLookup();
                List<String> names = lookup.getPreviousNames(player.getUniqueId());
                for (String n : names) {
                    if (n.equalsIgnoreCase(name)) { // match!
                        if (DEBUG) {
                            log.info("[Lockette] Found the match in the name history!");
                        }

                        signUtil.setLine(sign, index, player.getName(), player);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.info("[Lockette] Something bad happened returning match = false");
            e.printStackTrace();
        }

        return false;
    }

    // need to put this back in because others might be using it.
    public boolean isOwner(Block block, String name) {
        return isOwner(block, Bukkit.getOfflinePlayer(name));
    }

    public boolean isUser(Block block, String name, boolean withGroups) {
        return isUser(block, Bukkit.getOfflinePlayer(name), withGroups);
    }

    public boolean isOwner(Block block, OfflinePlayer player) {
        if (!enabled) {
            return true;
        }

        Block checkBlock = findBlockOwner(block);
        if (checkBlock == null) {
            return true;
        }

        Sign sign = (Sign) checkBlock.getState();

        // Check owner only.
        return matchUserUUID(sign, 1, player, true);
    }

    public boolean isOwner(Sign sign, OfflinePlayer player) {
        // Check owner only.
        return matchUserUUID(sign, 1, player, true);
    }

    public boolean isUser(Block block, OfflinePlayer player, boolean withGroups) {
        if (!enabled) {
            return true;
        }

        Block signBlock = findBlockOwner(block);

        if (signBlock == null) {
            return true;
        }

        // Check main three users.
        Sign sign = (Sign) signBlock.getState();

        for (int y = 1; y <= 3; ++y) {
            String line = sign.getLine(y);
            if (matchUserUUID(sign, y, player, true)) {// Check if the name is there verbatum.
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
        List<Block> list = findBlockUsers(block, signBlock);
        for (Block blk : list) {
            sign = (Sign) blk.getState();

            for (int y = 1; y <= 3; y++) {
                String line = sign.getLine(y);
                if (matchUserUUID(sign, y, player, true)) {// Check if the name is there verbatum.
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


    public MutableBoolean getUUIDSupport() {
        return uuidSupport;
    }
    
    public LocketteProperties getLocketteProperties() {
        return properties;
    }

    private void setStuff() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("[")
            .append(getDescription().getName())
        .append("]");
        logName = builder.toString();
        version = getDescription().getVersion();
    }
    public String getMETA_KEY() {
        return META_KEY;
    }
    public boolean getDEBUG() {
        return DEBUG;
    }
}
