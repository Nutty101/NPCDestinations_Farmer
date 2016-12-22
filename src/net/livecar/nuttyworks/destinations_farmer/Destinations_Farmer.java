package net.livecar.nuttyworks.destinations_farmer;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import net.citizensnpcs.Citizens;
import net.livecar.nuttyworks.destinations_farmer.plugin.Farmer_Processing;
import net.livecar.nuttyworks.destinations_farmer.storage.Location_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting;
import net.livecar.nuttyworks.destinations_farmer.worldguard.WorldGuard_Plugin;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

public class Destinations_Farmer 
{
	//For quick reference to this instance of the plugin.
	public static Destinations_Farmer Instance = null;

	//Links to classes
	public Citizens getCitizensPlugin;
	public Farmer_Processing getProcessingClass;
	public WorldGuard_Plugin getWorldGuardPlugin = null;
	public DestinationsPlugin getDestinationsPlugin = null;
	public DestFarmer_Plugin getPluginReference = null;
	
	//variables
	public int Version = 10000;
	public int entityRadius = 47*47;

	public Map<Integer,NPC_Setting> npcSettings = new HashMap<Integer,NPC_Setting>();
	public Map<Integer,Location_Setting> monitoredNPCs = new HashMap<Integer,Location_Setting>();

	//Storage locations
	public File languagePath;

	void getDefaultConfigs() 
	{
		//Create the default folders
		if (!DestinationsPlugin.Instance.getDataFolder().exists())
			DestinationsPlugin.Instance.getDataFolder().mkdirs();
		if (!languagePath.exists())
			languagePath.mkdirs();


		//Validate that the default package is in the MountPackages folder. If not, create it.
		exportConfig(languagePath, "en_def-farmer.yml");

	}
	void exportConfig(File path,String filename)
	{
		DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "nuDestinationsFarmer.exportConfig()|");
		File fileConfig = new File(path, filename);
		if (!fileConfig.isDirectory())
		{
			//Reader defConfigStream = null;
			try 
			{
			  FileUtils.copyURLToFile((URL)getClass().getResource("/" + filename), fileConfig);
			} 
			catch (IOException e1) 
			{
				if (getDestinationsPlugin != null)
					DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.SEVERE,
							"nuDestinationsFarmer.exportConfig()|FailedToExtractFile(" + filename + ")");
				else 
					logToConsole(" Failed to extract default file (" + filename + ")");
				return;
			}
		}
	}
	public void logToConsole(String logLine)
	{
		Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + DestinationsPlugin.Instance.getDescription().getName() + "] " 
			+ logLine);
	}
}
