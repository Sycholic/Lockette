/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yi.acru.bukkit.Lockette;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import static org.yi.acru.bukkit.PluginCore.log;

/**
 *
 * @author Samuel
 */
public class LocketteProperties {

    private final Lockette plugin;
    public LocketteProperties(Lockette instance) {
        this.plugin = instance;
    }
    
    
    
        //@SuppressWarnings("unchecked") Not needed anymore i guess.
    public void loadProperties(boolean reload) {
        if (reload) {
            log.log(Level.INFO, "{0} Reloading plugin configuration files.", plugin.logName);
            plugin.reloadConfig();
        }

        FileConfiguration properties = plugin.getConfig();
        boolean propChanged = true;
        //boolean			tempBoolean;

        plugin.getUUIDSupport().setValue(properties.getBoolean("enable-uuid-support", false));
        properties.set("enable-uuid-support", plugin.getUUIDSupport().booleanValue());
        plugin.msgUser = properties.getBoolean("enable-messages-user", true);
        properties.set("enable-messages-user", plugin.msgUser);
        plugin.msgOwner = properties.getBoolean("enable-messages-owner", false);
        properties.set("enable-messages-owner", plugin.msgOwner);
        //msgAdmin = true;
        plugin.msgAdmin = properties.getBoolean("enable-messages-admin", true);
        properties.set("enable-messages-admin", plugin.msgAdmin);
        plugin.msgError = properties.getBoolean("enable-messages-error", true);
        properties.set("enable-messages-error", plugin.msgError);
        plugin.msgHelp = properties.getBoolean("enable-messages-help", true);
        properties.set("enable-messages-help", plugin.msgHelp);

        plugin.explosionProtectionAll = properties.getBoolean("explosion-protection-all", true);
        properties.set("explosion-protection-all", plugin.explosionProtectionAll);
        plugin.rotateChests = properties.getBoolean("enable-chest-rotation", false);
        properties.set("enable-chest-rotation", plugin.rotateChests);

        plugin.usePermissions = properties.getBoolean("enable-permissions", false);
        properties.set("enable-permissions", plugin.usePermissions);
        plugin.protectDoors = properties.getBoolean("enable-protection-doors", true);
        properties.set("enable-protection-doors", plugin.protectDoors);
        plugin.protectTrapDoors = properties.getBoolean("enable-protection-trapdoors", true);
        properties.set("enable-protection-trapdoors", plugin.protectTrapDoors);

        plugin.adminSnoop = properties.getBoolean("allow-admin-snoop", false);
        properties.set("allow-admin-snoop", plugin.adminSnoop);
        plugin.adminBypass = properties.getBoolean("allow-admin-bypass", true);
        properties.set("allow-admin-bypass", plugin.adminBypass);
        plugin.adminBreak = properties.getBoolean("allow-admin-break", true);
        properties.set("allow-admin-break", plugin.adminBreak);

        plugin.blockHopper = properties.getBoolean("enable-hopper-blocking", true);
        properties.set("enable-hopper-blocking", plugin.blockHopper);

        // Start a scheduled task, for closing doors.
        if (plugin.protectDoors || plugin.protectTrapDoors) {
            if (plugin.doorCloser.start()) {
                log.log(Level.SEVERE, "{0} Failed to register door closing task!", plugin.logName);
            }
        } else {
            plugin.doorCloser.stop();
        }

        plugin.directPlacement = properties.getBoolean("enable-quick-protect", true);
        properties.set("enable-quick-protect", plugin.directPlacement);
        plugin.colorTags = properties.getBoolean("enable-color-tags", true);
        properties.set("enable-color-tags", plugin.colorTags);

        // Don't write this option back out if it doesn't exist, and write a warning if it is enabled.
        plugin.debugMode = properties.getBoolean("enable-debug", false);
        if (plugin.debugMode) {
            log.log(Level.WARNING, "{0} Debug mode is enabled, so Lockette chests are NOT secure.", plugin.logName);
        }

        //directPlacement = true;
        // = properties.getBoolean("", true);
        //properties.set("", );
        //tempBoolean = properties.getBoolean("use-whitelist", false);
        //tempBoolean = properties.getBoolean("lock-all-chests", true);//rename
        //tempBoolean = properties.getBoolean("test-bool", true);
        //properties.set("test-bool", tempBoolean);
        plugin.defaultDoorTimer = properties.getInt("default-door-timer", -1);
        if (plugin.defaultDoorTimer == -1) {
            plugin.defaultDoorTimer = 0;
            properties.set("default-door-timer", plugin.defaultDoorTimer);
            propChanged = true;
        }

        // Customizable protected block list.
        plugin.customBlockList = (List<Object>) properties.getList("custom-lockable-block-list");
        if (plugin.customBlockList == null) {
            plugin.customBlockList = new ArrayList<>(3);
            plugin.customBlockList.add(Material.ENCHANTMENT_TABLE.getId());
            plugin.customBlockList.add(Material.JUKEBOX.getId());
            plugin.customBlockList.add(Material.DIAMOND_BLOCK.getId());
            plugin.customBlockList.add(Material.ANVIL.getId());
            plugin.customBlockList.add(Material.HOPPER.getId());
            properties.set("custom-lockable-block-list", plugin.customBlockList);
            propChanged = true;
        }
        if (!plugin.customBlockList.isEmpty()) {
            log.log(Level.INFO, "{0} Custom lockable block list: {1}", new Object[]{plugin.logName, plugin.customBlockList.toString()});
        }

        // Customizable disabled plugin link list.
        plugin.disabledPluginList = (List<Object>) properties.getList("linked-plugin-ignore-list");
        if (plugin.disabledPluginList == null) {
            plugin.disabledPluginList = new ArrayList<>(1);
            plugin.disabledPluginList.add("mcMMO");
            properties.set("linked-plugin-ignore-list", plugin.disabledPluginList);
            propChanged = true;
        }
        if (!plugin.disabledPluginList.isEmpty()) {
            log.log(Level.INFO, "{0} Ignoring linked plugins: {1}", new Object[]{plugin.logName, plugin.disabledPluginList.toString()});
        }

        plugin.broadcastSnoopTarget = properties.getString("broadcast-snoop-target");
        if (plugin.broadcastSnoopTarget == null) {
            plugin.broadcastSnoopTarget = "[Everyone]";
            properties.set("broadcast-snoop-target", plugin.broadcastSnoopTarget);
            propChanged = true;
        }
        plugin.broadcastBreakTarget = properties.getString("broadcast-break-target");
        if (plugin.broadcastBreakTarget == null) {
            plugin.broadcastBreakTarget = "[Everyone]";
            properties.set("broadcast-break-target", plugin.broadcastBreakTarget);
            propChanged = true;
        }
        plugin.broadcastReloadTarget = properties.getString("broadcast-reload-target");
        if (plugin.broadcastReloadTarget == null) {
            plugin.broadcastReloadTarget = "[Operators]";
            properties.set("broadcast-reload-target", plugin.broadcastReloadTarget);
            propChanged = true;
        }

        String stringsFileName = properties.getString("strings-file-name");
        if ((stringsFileName == null) || stringsFileName.isEmpty()) {
            stringsFileName = "strings-en.yml";
            properties.set("strings-file-name", stringsFileName);
            propChanged = true;
        }

        if (propChanged) {
            plugin.saveConfig();
        }
        loadStrings(reload, stringsFileName);
    }

    protected void loadStrings(boolean reload, String fileName) {
        boolean stringChanged = false;
        String tempString;
        File stringsFile = new File(plugin.getDataFolder(), fileName);

        // Close the strings file if already loaded.
        if (plugin.strings != null) {
            // Should automatically garbage collect.
            plugin.strings = null;
        }

        // Load the strings file.
        plugin.strings = new YamlConfiguration();
        try {
            plugin.strings.load(stringsFile);
        } catch (InvalidConfigurationException ex) {
            log.log(Level.WARNING, "{0} Error loading {1}: {2}", new Object[]{plugin.logName, fileName, ex.getMessage()});

            if (!fileName.equals("strings-en.yml")) {
                loadStrings(reload, "strings-en.yml");
                return;
            } else {
                log.log(Level.WARNING, "{0} Returning to default strings.", plugin.logName);
            }
        } catch (IOException ex) {
        }

        // To remove French tags from the default strings file, and to not save to alternate strings files.
        boolean original = false;
        if (fileName.equals("strings-en.yml")) {
            original = true;

            plugin.strings.set("language", "English");

            // Force to be first.
            if (original) {
                try {
                    plugin.strings.save(stringsFile);
                    plugin.strings.load(stringsFile);
                } catch (IOException | InvalidConfigurationException ex) {
                }
            }

            plugin.strings.set("author", "Acru");
            plugin.strings.set("editors", "");
            plugin.strings.set("version", 0);
        }

        // Report language.
        tempString = plugin.strings.getString("language");
        if ((tempString == null) || tempString.isEmpty()) {
            log.log(Level.INFO, "{0} Loading strings file {1}", new Object[]{plugin.logName, fileName});
        } else {
            log.log(Level.INFO, "{0} Loading strings file for {1} by {2}", new Object[]{plugin.logName, tempString, plugin.strings.getString("author")});
        }

        // Load in the alternate sign strings.
        plugin.altPrivate = plugin.strings.getString("alternate-private-tag");
        if ((plugin.altPrivate == null) || plugin.altPrivate.isEmpty() || (original && plugin.altPrivate.equals("Privé"))) {
            plugin.altPrivate = "Private";
            plugin.strings.set("alternate-private-tag", plugin.altPrivate);
        }
        plugin.altPrivate = "[" + plugin.altPrivate + "]";

        plugin.altMoreUsers = plugin.strings.getString("alternate-moreusers-tag");
        if ((plugin.altMoreUsers == null) || plugin.altMoreUsers.isEmpty() || (original && plugin.altMoreUsers.equals("Autre Noms"))) {
            plugin.altMoreUsers = "More Users";
            plugin.strings.set("alternate-moreusers-tag", plugin.altMoreUsers);
            stringChanged = true;
        }
        plugin.altMoreUsers = "[" + plugin.altMoreUsers + "]";

        plugin.altEveryone = plugin.strings.getString("alternate-everyone-tag");
        if ((plugin.altEveryone == null) || plugin.altEveryone.isEmpty() || (original && plugin.altEveryone.equals("Tout le Monde"))) {
            plugin.altEveryone = "Everyone";
            plugin.strings.set("alternate-everyone-tag", plugin.altEveryone);
            stringChanged = true;
        }
        plugin.altEveryone = "[" + plugin.altEveryone + "]";

        plugin.altOperators = plugin.strings.getString("alternate-operators-tag");
        if ((plugin.altOperators == null) || plugin.altOperators.isEmpty() || (original && plugin.altOperators.equals("Opérateurs"))) {
            plugin.altOperators = "Operators";
            plugin.strings.set("alternate-operators-tag", plugin.altOperators);
            stringChanged = true;
        }
        plugin.altOperators = "[" + plugin.altOperators + "]";

        plugin.altTimer = plugin.strings.getString("alternate-timer-tag");
        if ((plugin.altTimer == null) || plugin.altTimer.isEmpty() || (original && plugin.altTimer.equals("Minuterie"))) {
            plugin.altTimer = "Timer";
            plugin.strings.set("alternate-timer-tag", plugin.altTimer);
            stringChanged = true;
        }

        plugin.altFee = plugin.strings.getString("alternate-fee-tag");
        if ((plugin.altFee == null) || plugin.altFee.isEmpty()) {
            plugin.altFee = "Fee";
            plugin.strings.set("alternate-fee-tag", plugin.altFee);
            stringChanged = true;
        }

        // Check all the message plugin.strings.
        // Messages for onBlockPlace.
        tempString = plugin.strings.getString("msg-user-conflict-door");
        if (tempString == null) {
            plugin.strings.set("msg-user-conflict-door", "Conflicting door removed!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-user-illegal");
        if (tempString == null) {
            plugin.strings.set("msg-user-illegal", "Illegal chest removed!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-user-resize-owned");
        if (tempString == null) {
            plugin.strings.set("msg-user-resize-owned", "You cannot resize a chest claimed by ***.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-help-chest");
        if (tempString == null) {
            plugin.strings.set("msg-help-chest", "Place a sign headed [Private] next to a chest to lock it.");
            stringChanged = true;
        }

        // Messages for onBlockBreak.
        tempString = plugin.strings.getString("msg-owner-release");
        if (tempString == null) {
            plugin.strings.set("msg-owner-release", "You have released a container!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-admin-release");
        if (tempString == null) {
            plugin.strings.set("msg-admin-release", "(Admin) @@@ has broken open a container owned by ***!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-user-release-owned");
        if (tempString == null) {
            plugin.strings.set("msg-user-release-owned", "You cannot release a container claimed by ***.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-owner-remove");
        if (tempString == null) {
            plugin.strings.set("msg-owner-remove", "You have removed users from a container!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-user-remove-owned");
        if (tempString == null) {
            plugin.strings.set("msg-user-remove-owned", "You cannot remove users from a container claimed by ***.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-user-break-owned");
        if (tempString == null) {
            plugin.strings.set("msg-user-break-owned", "You cannot break a container claimed by ***.");
            stringChanged = true;
        }

        // Messages for onBlockDamage.
        tempString = plugin.strings.getString("msg-user-denied-door");
        if (tempString == null) {
            plugin.strings.set("msg-user-denied-door", "You don't have permission to use this door.");
            stringChanged = true;
        }

        // Messages for onBlockRightClick.
        tempString = plugin.strings.getString("msg-user-touch-fee");
        if (tempString == null) {
            plugin.strings.set("msg-user-touch-fee", "A fee of ### will be paid to ***, to open.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-user-touch-owned");
        if (tempString == null) {
            plugin.strings.set("msg-user-touch-owned", "This container has been claimed by ***.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-help-select");
        if (tempString == null) {
            plugin.strings.set("msg-help-select", "Sign selected, use /lockette <line number> <text> to edit.");
            stringChanged = true;
        }

        // Messages for onBlockInteract.
        tempString = plugin.strings.getString("msg-admin-bypass");
        if (tempString == null) {
            plugin.strings.set("msg-admin-bypass", "Bypassed a door owned by ***, be sure to close it behind you.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-admin-snoop");
        if (tempString == null) {
            plugin.strings.set("msg-admin-snoop", "(Admin) @@@ has snooped around in a container owned by ***!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-user-denied");
        if (tempString == null) {
            plugin.strings.set("msg-user-denied", "You don't have permission to open this container.");
            stringChanged = true;
        }

        // Messages for onSignChange.
        tempString = plugin.strings.getString("msg-error-zone");
        if (tempString == null) {
            plugin.strings.set("msg-error-zone", "This zone is protected by ***.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-error-permission");
        if (tempString == null) {
            plugin.strings.set("msg-error-permission", "Permission to lock container denied.");
            stringChanged = true;
        } else if (tempString.equals("Permission to lock containers denied.")) {
            plugin.strings.set("msg-error-permission", "Permission to lock container denied.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-error-claim");
        if (tempString == null) {
            plugin.strings.set("msg-error-claim", "No unclaimed container nearby to make Private!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-error-claim-conflict");
        if (tempString == null) {
            plugin.strings.set("msg-error-claim-conflict", "Conflict with an existing protected door.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-admin-claim-error");
        if (tempString == null) {
            plugin.strings.set("msg-admin-claim-error", "Player *** is not online, be sure you have the correct name.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-admin-claim");
        if (tempString == null) {
            plugin.strings.set("msg-admin-claim", "You have claimed a container for ***.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-owner-claim");
        if (tempString == null) {
            plugin.strings.set("msg-owner-claim", "You have claimed a container!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-error-adduser-owned");
        if (tempString == null) {
            plugin.strings.set("msg-error-adduser-owned", "You cannot add users to a container claimed by ***.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-error-adduser");
        if (tempString == null) {
            plugin.strings.set("msg-error-adduser", "No claimed container nearby to add users to!");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-owner-adduser");
        if (tempString == null) {
            plugin.strings.set("msg-owner-adduser", "You have added users to a container!");
            stringChanged = true;
        }

        // Messages for onPlayerCommand.
        if (original) {
            plugin.strings.set("msg-help-command1", "&C/lockette <line number> <text> - Edits signs on locked containers. Right click on the sign to edit.");
            plugin.strings.set("msg-help-command2", "&C/lockette fix - Fixes an automatic door that is in the wrong position. Look at the door to edit.");
            plugin.strings.set("msg-help-command3", "&C/lockette reload - Reloads the configuration files. Operators only.");
            plugin.strings.set("msg-help-command4", "&C/lockette version - Reports Lockette version.");
            stringChanged = true;
        }

        /*
         tempString = strings.getString("msg-help-command1");
         if(tempString == null){
         strings.set("msg-help-command1", "/lockette reload - Reloads the configuration files.");
         stringChanged = true;
         }
         tempString = strings.getString("msg-help-command2");
         if(tempString == null){
         strings.set("msg-help-command2", "/lockette <line number> <text> - Edits signs on locked containers. Right click on the sign to edit.");
         stringChanged = true;
         }
         */
        tempString = plugin.strings.getString("msg-admin-reload");
        if (tempString == null) {
            plugin.strings.set("msg-admin-reload", "Reloading plugin configuration files.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-error-fix");
        if (tempString == null) {
            plugin.strings.set("msg-error-fix", "No owned door found.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-error-edit");
        if (tempString == null) {
            plugin.strings.set("msg-error-edit", "First select a sign by right clicking it.");
            stringChanged = true;
        }
        tempString = plugin.strings.getString("msg-owner-edit");
        if (tempString == null) {
            plugin.strings.set("msg-owner-edit", "Sign edited successfully.");
            stringChanged = true;
        }

        /*
		
         tempString = strings.getString("");
         if(tempString == null){
         strings.set("", "");
         stringChanged = true;
         }
		
         */
        if (original) {
            if (stringChanged) {
                try {
                    plugin.strings.save(stringsFile);
                } catch (Exception ex) {
                }
            }
        }
    }
    
}
