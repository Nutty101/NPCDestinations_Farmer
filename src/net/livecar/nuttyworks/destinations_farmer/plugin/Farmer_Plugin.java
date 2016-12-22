package net.livecar.nuttyworks.destinations_farmer.plugin;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.livecar.nuttyworks.destinations_farmer.Destinations_Farmer;
import net.livecar.nuttyworks.destinations_farmer.storage.Location_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting.CurrentAction;
import net.livecar.nuttyworks.npc_destinations.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;

public class Farmer_Plugin extends DestinationsAddon
{
	private Destinations_Farmer pluginReference = null;
	
	public Farmer_Plugin(Destinations_Farmer instanceRef)
	{
		pluginReference = instanceRef;
	}
	
	@Override
	public String getActionName() 
	{
		return "FARMING";
	}
	
	@Override
	public String getPluginIcon() 
	{
		return "♨";
	}
	
	@Override
	public String getCommandName() 
	{
		return "locfarmer"; 
	}

	@Override
	public String getQuickDescription() 
	{
		String[] response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer",
				"messages.plugin_description", "");
		return response[0];
	}

	@Override
	public String getDestinationHelp(NPC npc,NPCDestinationsTrait npcTrait, Destination_Setting location) 
	{
		String[] response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer",null, 
				"messages.plugin_destination", npcTrait, location, npc, null, 0);
		return response[0];
	}

	@Override
	public String getGenericHelp(CommandSender sender) 
	{
		String response = "";
		for (String message : pluginReference.getDestinationsPlugin.getMessageManager.buildMessage(
				"farmer","messages.help_command", "")) 
		{
			response += message;
		}
		return response;
	}

	@Override
	public String parseLanguageLine(String message, NPCDestinationsTrait npcTrait,Destination_Setting locationSetting
			,Material blockMaterial,NPC npc, int ident)
	{
		if (locationSetting != null)
		{
			if (pluginReference.npcSettings.containsKey(npc.getId()))
			{
				if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(locationSetting.LocationIdent)) 
				{
					if (message.toLowerCase().contains("<farmer."))
					{
						Location_Setting locSetting = pluginReference.npcSettings.get(npc.getId()).locations.get(
								locationSetting.LocationIdent);
						
						if (!locSetting.regionName.equals(""))
						{
							message = message.replaceAll("<farmer\\.value>", locSetting.regionName);
							message = message.replaceAll("<farmer\\.setting>", "Region Name");
							message = message.replaceAll("<farmer\\.replant>", (locSetting.plantExisting?
									pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer"
											, "result_messages.replant","")[0]:
										pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer"
												, "result_messages.replant_hand","")[0]));
						} 
						else if (locSetting.maxDistance > 0) 
						{
							message = message.replaceAll("<farmer\\.value>", Integer.toString(locSetting.maxDistance));
							message = message.replaceAll("<farmer\\.setting>", "Max Distance");
							message = message.replaceAll("<farmer\\.replant>", (locSetting.plantExisting?
									pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer"
											, "result_messages.replant","")[0]:
										pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("farmer"
												, "result_messages.replant_hand","")[0]));
						}
						else 
						{
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
	public boolean onUserCommand(CommandSender sender, NPC npc, NPCDestinationsTrait npcTrait, String[] inargs) 
	{
		boolean isOwner = false;
		if (npc.hasTrait(Owner.class)) 
		{
			if (sender instanceof Player) 
			{
				Owner ownerTrait = npc.getTrait(Owner.class);
				if (ownerTrait.isOwnedBy(sender)) 
				{
					isOwner = true;
				} 
			}
		}
		
		if (!sender.hasPermission("npcdestinations.editall.locfarmer") && !sender.isOp() 
				&& !(isOwner && sender.hasPermission("npcdestinations.editown.locfarmer"))) 
		{
			pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender, "messages.no_permissions");
			return true;
		}
		else 
		{
			if (inargs.length < 2)
			{
				pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender, "messages.command_badargs");
				return true;
			}
			
			int nIndex = Integer.parseInt(inargs[1]);
			if (nIndex > npcTrait.NPCLocations.size()-1) 
			{
				pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender, "messages.command_badargs");
				return true;
			}

			Destination_Setting destSetting = npcTrait.NPCLocations.get(nIndex);
			
			NPC_Setting farmSetting;
			if (!pluginReference.npcSettings.containsKey(npc.getId())) 
			{
				farmSetting  = new NPC_Setting();
				farmSetting.setNPC(npc.getId());
			} 
			else 
			{
				farmSetting = pluginReference.npcSettings.get(npc.getId());
			}

			if (inargs.length == 2) 
			{
				//Remove the settings, and detach this from the location.
				if (pluginReference.monitoredNPCs.containsKey(npc.getId())) 
				{
					if (pluginReference.monitoredNPCs.get(npc.getId()).locationID.equals(destSetting.LocationIdent)) 
					{
						pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO
								,"DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() 
								+ "|New Location,clearing monitors and releasing control.");
						npcTrait.unsetMonitoringPlugin();
						pluginReference.npcSettings.get(npc.getId()).currentAction = CurrentAction.IDLE;
						pluginReference.npcSettings.get(npc.getId()).currentDestination = null;
						pluginReference.monitoredNPCs.remove(npc.getId());
					}
				}
				if (farmSetting.locations.containsKey(destSetting.LocationIdent)) 
				{
					farmSetting.locations.remove(destSetting.LocationIdent);
					pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender
							,"messages.command_removed", npcTrait, destSetting);
				}
				return true;
			}
			
			Location_Setting locSetting;
			if (!farmSetting.locations.containsKey(destSetting.LocationIdent)) 
			{
				locSetting  = new Location_Setting();
				locSetting.locationID = destSetting.LocationIdent;
				farmSetting.locations.put(locSetting.locationID, locSetting);
			} 
			else 
			{
				locSetting = farmSetting.locations.get(destSetting.LocationIdent);
			}
			
			if (inargs[2].matches("\\d+")) 
			{
				//max distance
				locSetting.maxDistance = Integer.parseInt(inargs[2]);
				locSetting.regionName = "";
			} 
			else 
			{
				//Region name
				if (pluginReference.getWorldGuardPlugin == null) 
				{
					pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender
							, "messages.command_noworldguard");
					return true;
				} 
				else 
				{
					if (!pluginReference.getWorldGuardPlugin.getRegionManager(npc.getEntity().getWorld()).hasRegion(inargs[2])) 
					{
						pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender
								,"messages.command_invalidregion");
						return true;
					} 
					else 
					{
						locSetting.regionName = inargs[2];
						locSetting.maxDistance = 0;
					}
				}
			}
			
			if (inargs.length > 3)
			{
				if (inargs[3] == "replant")
					locSetting.plantExisting = true;
			}
			
			if (!pluginReference.npcSettings.containsKey(npc.getId())) 
			{
				pluginReference.npcSettings.put(npc.getId(), farmSetting);
				pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender, "messages.plugin_debug");
			}
			
			if (locSetting.locationID.equals(npcTrait.currentLocation.LocationIdent))
			{
				pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Farmer_Plugin.onUserCommand|NPC:"
						+ npc.getId() + "|Location added, starting monitor");
				NPCDestinationsTrait trait = npc.getTrait(NPCDestinationsTrait.class);
				trait.setMonitoringPlugin(pluginReference.getPluginReference, npcTrait.currentLocation);
				
				pluginReference.monitoredNPCs.put(npc.getId(), pluginReference.npcSettings.get(npc.getId()).locations.get(
						npcTrait.currentLocation.LocationIdent));
				
				pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender 
						, "messages.command_added_active", npcTrait, npcTrait.currentLocation);
			}
			else 
			{
				pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender
						, "messages.command_added_notactive", npcTrait, npcTrait.currentLocation);
			}
			return true;
		}
		
	}
	
	@Override
	public void onLocationLoading(NPC npc,NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) 
	{
		if (!storageKey.keyExists("Farmers"))
			return;

		NPC_Setting npcFarmer;
		if (!pluginReference.npcSettings.containsKey(npc.getId()))
		{
			npcFarmer = new NPC_Setting();
			npcFarmer.setNPC(npc.getId());
			pluginReference.npcSettings.put(npc.getId(), npcFarmer);
		} 
		else 
		{
			npcFarmer = pluginReference.npcSettings.get(npc.getId());
		}
		
		Location_Setting locationConfig = new Location_Setting();
		locationConfig.regionName = storageKey.getString("Farmers.region","");
		locationConfig.locationID = UUID.fromString(storageKey.getString("Farmers.LocationID",""));
		locationConfig.maxDistance = storageKey.getInt("Farmers.maxDistance",0);
		locationConfig.plantExisting = storageKey.getBoolean("Farmers.plantExisting",false);
		if (locationConfig.locationID != null)
			npcFarmer.locations.put(locationConfig.locationID, locationConfig);
	}
	
	@Override
	public void onLocationSaving(NPC npc,NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) 
	{
		if (!pluginReference.npcSettings.containsKey(npc.getId()))
			return;
		if (!pluginReference.npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent))
			return;
		
		Location_Setting farmerLocation = pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent);

		if (farmerLocation.locationID != null)
		{
			storageKey.setString("Farmers.LocationID",farmerLocation.locationID.toString());
			storageKey.setString("Farmers.region",farmerLocation.regionName);
			storageKey.setInt("Farmers.maxDistance",farmerLocation.maxDistance );
			storageKey.setBoolean("Farmers.plantExisting",farmerLocation.plantExisting);
		}
	}

	@Override
	public void onEnableChanged(NPC npc, NPCDestinationsTrait trait, boolean enabled) 
	{
		if (enabled)
		{
			if (pluginReference.npcSettings.containsKey(npc.getId()))
			{
				if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(trait.currentLocation.LocationIdent)) 
				{
					if (!pluginReference.monitoredNPCs.containsKey(npc.getId()))
					{
						pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO
								, "DestinationsEventsListener.onNavigationReached|NPC:" + npc.getId() 
								+ "|Monitored location reached, assigning as monitor");
						trait.setMonitoringPlugin(pluginReference.getPluginReference, trait.currentLocation);
						pluginReference.monitoredNPCs.put(npc.getId()
								, pluginReference.npcSettings.get(npc.getId()).locations.get(trait.currentLocation.LocationIdent));
						return;
					}
				}
			}
		} 
		else 
		{
			if (pluginReference.monitoredNPCs.containsKey(Integer.valueOf(npc.getId())))
			{
				if ((npc.getEntity().getLocation().getBlockX() != trait.currentLocation.destination.getBlockX()) 
						|| (npc.getEntity().getLocation().getBlockZ() != trait.currentLocation.destination.getBlockZ()))
				{
					pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO
							, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() 
							+ "|plugin disabled for this npc, aborting and returning to destination");
					((NPC_Setting)pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.ABORTING;
					return;
				}
				pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, 
						"DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() 
						+ "|plugin disabled for this npc, removing monitors.");
				trait.unsetMonitoringPlugin();
				((NPC_Setting)pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.IDLE;
				((NPC_Setting)pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentDestination = null;
				pluginReference.monitoredNPCs.remove(Integer.valueOf(npc.getId()));
			}
		}
	}

	@Override
	public boolean onNavigationReached(NPC npc, NPCDestinationsTrait trait, Destination_Setting destination) 
	{
		if (pluginReference.npcSettings.containsKey(npc.getId()))
		{
			if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(destination.LocationIdent)) {
				//Event triggered
				if (!pluginReference.monitoredNPCs.containsKey(npc.getId())) 
				{
					pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, assigning as monitor");
					trait.setMonitoringPlugin(pluginReference.getPluginReference, destination);
					pluginReference.monitoredNPCs.put(npc.getId(), pluginReference.npcSettings.get(npc.getId()).locations.get(destination.LocationIdent));
				}
			}
		}
		return false;
	}

	@Override
	public boolean onNewDestination(NPC npc, NPCDestinationsTrait trait, Destination_Setting destination) 
	{
		if (pluginReference.npcSettings.containsKey(Integer.valueOf(npc.getId())))
		{
			Location entityLocation = npc.getEntity().getLocation();
			Location curDestLoc = trait.currentLocation.destination;
			if (((NPC_Setting)pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).locations.containsKey(
					trait.currentLocation.LocationIdent)) 
			{
				if ((entityLocation.getBlockX() != curDestLoc.getBlockX()) || (entityLocation.getBlockZ() != curDestLoc.getBlockZ()))
				{
					pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO
							, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() 
							+ "|New Location, not at start, aborting.");
					((NPC_Setting)pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.ABORTING;
					return true;
				}
				else if (pluginReference.monitoredNPCs.containsKey(Integer.valueOf(npc.getId())))
				{
					pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO
							, "DestinationsEventsListener.onNavigationNewDestination|NPC:" + npc.getId() 
							+ "|New Location,clearing monitors and releasing control.");
					trait.unsetMonitoringPlugin();
					((NPC_Setting)pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentAction = NPC_Setting.CurrentAction.IDLE;
					((NPC_Setting)pluginReference.npcSettings.get(Integer.valueOf(npc.getId()))).currentDestination = null;
					pluginReference.monitoredNPCs.remove(Integer.valueOf(npc.getId()));
				}
			}
		}
		return false;
	}
}
