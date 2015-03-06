/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yi.acru.bukkit.Lockette.Utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.yi.acru.bukkit.BlockUtil;
//import org.yi.acru.bukkit.Lockette.Lockette;

/**
 *
 * @author Samuel
 */
public class DoorUtils {

//    private final Lockette plugin;
//
//    public DoorUtils(Lockette instance) {
//        this.plugin = instance;
//    }
    // Toggle one door.  (Used only by pre-561 builds to fix for bug.)
    // Now also used to fix doors.
    public void toggleSingleDoor(Block block) {
        int type = block.getTypeId();
        //List<Block> list = new ArrayList<Block>();

        if (BlockUtil.isInList(type, BlockUtil.materialListJustDoors)) {
            toggleDoorBase(block, null, true, false, null);
        } else if (BlockUtil.isInList(type, BlockUtil.materialListTrapDoors)
                || BlockUtil.isInList(type, BlockUtil.materialListGates)) {
            toggleDoorBase(block, null, false, false, null);
        }
        //return(list);
    }

    // Toggle half door, or trap door.  (Used by automatic door closer.)
    public void toggleHalfDoor(Block block, boolean effect) {
        int type = block.getTypeId();
		//List<Block> list = new ArrayList<Block>();

        //toggleDoor(block, null, false, false, null);
        //return(list);
        if (BlockUtil.isInList(type, BlockUtil.materialListDoors)) {
            block.setData((byte) (block.getData() ^ 4));
            try {
                if (effect) {
                    block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
                }
            } catch (NoSuchFieldError | NoSuchMethodError | NoClassDefFoundError ex) {
            }
        }
    }

    // Main recursive function for toggling a door pair.  (No good for trap doors.)
    public void toggleDoorBase(Block block, Block keyBlock, boolean iterateUpDown, boolean skipDoor, List<Block> list) {
        Block checkBlock;

        // Toggle this door.
        if (list != null) {
            list.add(block);
        }
        if (!skipDoor) {
            block.setData((byte) (block.getData() ^ 4));
        }

        // Check up and down.
        if (iterateUpDown) {
            checkBlock = block.getRelative(BlockFace.UP);
            if (checkBlock.getTypeId() == block.getTypeId()) {
                toggleDoorBase(checkBlock, null, false, skipDoor, list);
            }

            checkBlock = block.getRelative(BlockFace.DOWN);
            if (checkBlock.getTypeId() == block.getTypeId()) {
                toggleDoorBase(checkBlock, null, false, skipDoor, list);
            }
        }

        // Check around the originating block, in the order NESW.
        if (keyBlock != null) {
            checkBlock = block.getRelative(BlockFace.NORTH);
            if (checkBlock.getTypeId() == block.getTypeId()) {
                if (((checkBlock.getX() == keyBlock.getX()) && (checkBlock.getZ() == keyBlock.getZ()))
                        || ((block.getX() == keyBlock.getX()) && (block.getZ() == keyBlock.getZ()))) {
                    toggleDoorBase(checkBlock, null, true, false, list);
                }
            }

            checkBlock = block.getRelative(BlockFace.EAST);
            if (checkBlock.getTypeId() == block.getTypeId()) {
                if (((checkBlock.getX() == keyBlock.getX()) && (checkBlock.getZ() == keyBlock.getZ()))
                        || ((block.getX() == keyBlock.getX()) && (block.getZ() == keyBlock.getZ()))) {
                    toggleDoorBase(checkBlock, null, true, false, list);
                }
            }

            checkBlock = block.getRelative(BlockFace.SOUTH);
            if (checkBlock.getTypeId() == block.getTypeId()) {
                if (((checkBlock.getX() == keyBlock.getX()) && (checkBlock.getZ() == keyBlock.getZ()))
                        || ((block.getX() == keyBlock.getX()) && (block.getZ() == keyBlock.getZ()))) {
                    toggleDoorBase(checkBlock, null, true, false, list);
                }
            }

            checkBlock = block.getRelative(BlockFace.WEST);
            if (checkBlock.getTypeId() == block.getTypeId()) {
                if (((checkBlock.getX() == keyBlock.getX()) && (checkBlock.getZ() == keyBlock.getZ()))
                        || ((block.getX() == keyBlock.getX()) && (block.getZ() == keyBlock.getZ()))) {
                    toggleDoorBase(checkBlock, null, true, false, list);
                }
            }
        }
    }

    // Toggle all doors.  (Used by rightclick action to get door list.)
    public List<Block> toggleDoors(Block block, Block keyBlock, boolean wooden, boolean trap) {
        List<Block> list = new ArrayList<>();

        toggleDoorBase(block, keyBlock, !trap, wooden, list);
        try {
            if (!wooden) {
                block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
            }
        } catch (NoSuchFieldError | NoSuchMethodError | NoClassDefFoundError ex) {
        }

        return (list);
    }
}
