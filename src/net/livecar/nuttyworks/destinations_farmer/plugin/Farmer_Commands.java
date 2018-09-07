package net.livecar.nuttyworks.destinations_farmer.plugin;

import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.destinations_farmer.storage.Location_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting.CurrentAction;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.CommandInfo;

public class Farmer_Commands {
    @CommandInfo(
            name = "locfarmer", 
            group = "External Plugin Commands", 
            languageFile = "farmer", 
            helpMessage = "command_locfarmer_help", 
            arguments = { "#", "<region>|#" }, 
            permission = { "npcdestinations.editall.locfarmer", "npcdestinations.editown.locfarmer" }, 
            allowConsole = true, 
            minArguments = 2, 
            maxArguments = 3)
    public boolean npcDest_locfarmer(DestinationsPlugin destRef, CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait) {
        if (inargs.length < 2) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.command_badargs");
            return true;
        }

        int nIndex = Integer.parseInt(inargs[1]);
        if (nIndex > destTrait.NPCLocations.size() - 1) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
            return true;
        }

        Farmer_Plugin addonReference = (Farmer_Plugin) destRef.getPluginManager.getPluginByName("Farming");

        Destination_Setting destSetting = destTrait.NPCLocations.get(nIndex);
        NPC_Setting farmSetting;
        if (!addonReference.pluginReference.npcSettings.containsKey(npc.getId())) {
            farmSetting = new NPC_Setting();
            farmSetting.setNPC(npc.getId());
        } else {
            farmSetting = addonReference.pluginReference.npcSettings.get(npc.getId());
        }

        if (inargs.length == 2) {
            // Remove the settings, and detach this from the location.
            if (addonReference.pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                if (addonReference.pluginReference.monitoredNPCs.get(npc.getId()).locationID.equals(destSetting.LocationIdent)) {
                    addonReference.pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId()
                            + "|New Location,clearing monitors and releasing control.");
                    destTrait.unsetMonitoringPlugin();
                    addonReference.pluginReference.npcSettings.get(npc.getId()).currentAction = CurrentAction.IDLE;
                    addonReference.pluginReference.npcSettings.get(npc.getId()).currentDestination = null;
                    addonReference.pluginReference.monitoredNPCs.remove(npc.getId());
                }
            }
            if (farmSetting.locations.containsKey(destSetting.LocationIdent)) {
                farmSetting.locations.remove(destSetting.LocationIdent);
                addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer", sender, "messages.command_removed", destTrait, destSetting);
            }
            return true;
        }

        Location_Setting locSetting;
        if (!farmSetting.locations.containsKey(destSetting.LocationIdent)) {
            locSetting = new Location_Setting();
            locSetting.locationID = destSetting.LocationIdent;
            farmSetting.locations.put(locSetting.locationID, locSetting);
        } else {
            locSetting = farmSetting.locations.get(destSetting.LocationIdent);
        }

        if (inargs[2].matches("\\d+")) {
            // max distance
            locSetting.maxDistance = Integer.parseInt(inargs[2]);
            locSetting.regionName = "";
        } else {
            // Region name
            if (addonReference.pluginReference.getWorldGuardPlugin == null) {
                addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer", sender, "messages.command_noworldguard");
                return true;
            } else {
                if (!addonReference.pluginReference.getWorldGuardPlugin.getRegionManager(npc.isSpawned()?npc.getEntity().getWorld():((Player)sender).getLocation().getWorld()).hasRegion(inargs[2])) {
                    addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer", sender, "messages.command_invalidregion");
                    return true;
                } else {
                    locSetting.regionName = inargs[2];
                    locSetting.maxDistance = 0;
                }
            }
        }

        if (inargs.length > 3) {
            if (inargs[3].equalsIgnoreCase("replant"))
                locSetting.plantExisting = true;
        }

        if (!addonReference.pluginReference.npcSettings.containsKey(npc.getId())) {
            addonReference.pluginReference.npcSettings.put(npc.getId(), farmSetting);
            addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer", sender, "messages.plugin_debug");
        }

        if (locSetting.locationID.equals(destTrait.currentLocation.LocationIdent)) {
            addonReference.pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Farmer_Plugin.onUserCommand|NPC:" + npc.getId() + "|Location added, starting monitor");
            NPCDestinationsTrait trait = npc.getTrait(NPCDestinationsTrait.class);
            trait.setMonitoringPlugin(addonReference.pluginReference.getPluginReference, destTrait.currentLocation);

            addonReference.pluginReference.monitoredNPCs.put(npc.getId(), addonReference.pluginReference.npcSettings.get(npc.getId()).locations.get(destTrait.currentLocation.LocationIdent));

            addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer", sender, "messages.command_added_active", destTrait, destTrait.currentLocation);
        } else {
            addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer", sender, "messages.command_added_notactive", destTrait, destTrait.currentLocation);
        }
        return true;
    }
}
