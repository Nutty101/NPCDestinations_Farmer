package net.livecar.nuttyworks.destinations_farmer.plugin;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.livecar.nuttyworks.destinations_farmer.Destinations_Farmer;
import net.livecar.nuttyworks.destinations_farmer.storage.Location_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;

public class Farmer_Plugin extends DestinationsAddon {
    Destinations_Farmer pluginReference = null;

    public Farmer_Plugin(Destinations_Farmer instanceRef) {
        pluginReference = instanceRef;
    }

    @Override
    public String getActionName() {
        return "Farming";
    }

    @Override
    public String getPluginIcon() {
        return "♨";
    }

    @Override
    public String getQuickDescription() {
        String[] response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer", "messages.plugin_description", "");
        return response[0];
    }

    @Override
    public String getDestinationHelp(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        String[] response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer", null, "messages.plugin_destination", npcTrait, location, npc, null, 0);
        return response[0];
    }

    @Override
    public String parseLanguageLine(String message, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, Material blockMaterial, NPC npc, int ident) {
        if (locationSetting != null) {
            if (pluginReference.npcSettings.containsKey(npc.getId())) {
                if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(locationSetting.LocationIdent)) {
                    if (message.toLowerCase().contains("<farmer.")) {
                        Location_Setting locSetting = pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent);

                        if (!locSetting.regionName.equals("")) {
                            message = message.replaceAll("<farmer\\.value>", locSetting.regionName);
                            message = message.replaceAll("<farmer\\.setting>", "Region Name");
                            message = message.replaceAll("<farmer\\.replant>", (locSetting.plantExisting ? pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer", "result_messages.replant", "")[0]
                                    : pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer", "result_messages.replant_hand", "")[0]));
                        } else if (locSetting.maxDistance > 0) {
                            message = message.replaceAll("<farmer\\.value>", Integer.toString(locSetting.maxDistance));
                            message = message.replaceAll("<farmer\\.setting>", "Max Distance");
                            message = message.replaceAll("<farmer\\.replant>", (locSetting.plantExisting ? pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer", "result_messages.replant", "")[0]
                                    : pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer", "result_messages.replant_hand", "")[0]));
                        } else {
                            message = message.replaceAll("<farmer\\.value>", "Not Set");
                            message = message.replaceAll("<farmer\\.setting>", "Max / Region");
                            message = message.replaceAll("<farmer\\.replant>", "Not set");
                        }
                    }
                }
            }
        }

        if (message.toLowerCase().contains("<farmer.replant>"))
            message = message.replaceAll("<farmer\\.replant>", "Not set");
        if (message.toLowerCase().contains("<farmer.value>"))
            message = message.replaceAll("<farmer\\.value>", "Not set");
        if (message.toLowerCase().contains("<farmer.setting>"))
            message = message.replaceAll("<farmer\\.setting>", "Not set");

        return message;
    }

    @Override
    public void onLocationLoading(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!storageKey.keyExists("Farmers"))
            return;

        NPC_Setting npcFarmer;
        if (!pluginReference.npcSettings.containsKey(npc.getId())) {
            npcFarmer = new NPC_Setting();
            npcFarmer.setNPC(npc.getId());
            pluginReference.npcSettings.put(npc.getId(), npcFarmer);
        } else {
            npcFarmer = pluginReference.npcSettings.get(npc.getId());
        }

        Location_Setting locationConfig = new Location_Setting();
        locationConfig.regionName = storageKey.getString("Farmers.region", "");
        locationConfig.maxDistance = storageKey.getInt("Farmers.maxDistance", 0);
        locationConfig.plantExisting = storageKey.getBoolean("Farmers.plantExisting", false);

        locationConfig.locationID = location.LocationIdent;
        npcFarmer.locations.put(locationConfig.locationID, locationConfig);
    }

    @Override
    public void onLocationSaving(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!pluginReference.npcSettings.containsKey(npc.getId()))
            return;
        if (!pluginReference.npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent))
            return;

        Location_Setting farmerLocation = pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent);

        if (farmerLocation.locationID != null) {
            storageKey.setString("Farmers.region", farmerLocation.regionName);
            storageKey.setInt("Farmers.maxDistance", farmerLocation.maxDistance);
            storageKey.setBoolean("Farmers.plantExisting", farmerLocation.plantExisting);
        }
    }

    @Override
    public void onEnableChanged(NPC npc, NPCDestinationsTrait trait, boolean enabled) {
        if (enabled) {
            if (pluginReference.npcSettings.containsKey(npc.getId())) {
                if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(trait.currentLocation.LocationIdent)) {
                    if (!pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                        pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, assigning as monitor");
                        trait.setMonitoringPlugin(pluginReference.getPluginReference, trait.currentLocation);
                        pluginReference.monitoredNPCs.put(npc.getId(), pluginReference.npcSettings.get(npc.getId()).locations.get(trait.currentLocation.LocationIdent));
                        return;
                    }
                }
            }
        } else {
            if (pluginReference.monitoredNPCs.containsKey(Integer.valueOf(npc.getId()))) {
                if ((npc.getEntity().getLocation().getBlockX() != trait.currentLocation.destination.getBlockX()) || (npc.getEntity().getLocation().getBlockZ() != trait.currentLocation.destination.getBlockZ())) {
                    pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId()
                            + "|plugin disabled for this npc, aborting and returning to destination");
                    ((NPC_Setting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.ABORTING;
                    return;
                }
                pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() + "|plugin disabled for this npc, removing monitors.");
                trait.unsetMonitoringPlugin();
                ((NPC_Setting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.IDLE;
                ((NPC_Setting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentDestination = null;
                pluginReference.monitoredNPCs.remove(Integer.valueOf(npc.getId()));
            }
        }
    }

    @Override
    public boolean onNavigationReached(NPC npc, NPCDestinationsTrait trait, Destination_Setting destination) {
        if (pluginReference.npcSettings.containsKey(npc.getId())) {
            if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(destination.LocationIdent)) {
                // Event triggered
                if (!pluginReference.monitoredNPCs.containsKey(npc.getId())) {
                    pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, assigning as monitor");
                    trait.setMonitoringPlugin(pluginReference.getPluginReference, destination);
                    pluginReference.monitoredNPCs.put(npc.getId(), pluginReference.npcSettings.get(npc.getId()).locations.get(destination.LocationIdent));
                    ((NPC_Setting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.IDLE;
                }
            } else
            {
                //Undo the monitoring for this NPC
                if (pluginReference.monitoredNPCs.containsKey(npc.getId()))
                    pluginReference.monitoredNPCs.remove(npc.getId());
                trait.unsetMonitoringPlugin();
            }
        }
        return false;
    }

    @Override
    public boolean onNewDestination(NPC npc, NPCDestinationsTrait trait, Destination_Setting destination) {
        if (pluginReference.npcSettings.containsKey(Integer.valueOf(npc.getId()))) {
            Location entityLocation = npc.getEntity().getLocation();
            Location curDestLoc = trait.currentLocation.destination;
            if (((NPC_Setting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).locations.containsKey(trait.currentLocation.LocationIdent)) {
                if ((entityLocation.getBlockX() != curDestLoc.getBlockX()) || (entityLocation.getBlockZ() != curDestLoc.getBlockZ())) {
                    pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() + "|New Location, not at start, aborting.");
                    ((NPC_Setting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.ABORTING;
                    return true;
                } else if (pluginReference.monitoredNPCs.containsKey(Integer.valueOf(npc.getId()))) {
                    pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() + "|New Location,clearing monitors and releasing control.");
                    trait.unsetMonitoringPlugin();
                    ((NPC_Setting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.IDLE;
                    ((NPC_Setting) pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentDestination = null;
                    pluginReference.monitoredNPCs.remove(Integer.valueOf(npc.getId()));
                }
            }
        }
        return false;
    }
}
