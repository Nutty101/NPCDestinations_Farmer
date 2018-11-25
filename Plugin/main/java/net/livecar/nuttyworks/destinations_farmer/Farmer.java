package net.livecar.nuttyworks.destinations_farmer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import net.citizensnpcs.Citizens;
import net.livecar.nuttyworks.destinations_farmer.plugin.plugin.VersionInterface;
import net.livecar.nuttyworks.destinations_farmer.plugin.Processing;
import net.livecar.nuttyworks.destinations_farmer.plugin.MC_1_12_R1;
import net.livecar.nuttyworks.destinations_farmer.plugin.MC_1_13_R2;
import net.livecar.nuttyworks.destinations_farmer.plugin.Commands;
import net.livecar.nuttyworks.destinations_farmer.plugin.PluginExtension;
import net.livecar.nuttyworks.destinations_farmer.plugin.storage.Location_Setting;
import net.livecar.nuttyworks.destinations_farmer.plugin.storage.NPC_Setting;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

public class Farmer {
    // For quick reference to this instance of the plugin.
    public static Farmer                  Instance              = null;

    // For quick reference to this instance of the plugin.
    public FileConfiguration              getDefaultConfig;

    // Links to classes
    public Citizens                       getCitizensPlugin;
    public DestinationsPlugin             getDestinationsPlugin = null;
    public BukkitPlugin                   getPluginReference    = null;
    public Processing                     getProcessingClass    = null;
    public VersionInterface               getBridge             = null;
    public PluginExtension                getFarmerPlugin       = null;

    // variables
    public int                            Version               = 10000;
    public int                            entityRadius          = 47 * 47;

    public Map<Integer, NPC_Setting>      npcSettings           = new HashMap<Integer, NPC_Setting>();
    public Map<Integer, Location_Setting> monitoredNPCs         = new ConcurrentHashMap<Integer, Location_Setting>();

    // Storage locations
    public File                           languagePath;

    public Farmer() {
        this.getFarmerPlugin = new PluginExtension(this);
        DestinationsPlugin.Instance.getPluginManager.registerPlugin(getFarmerPlugin);
        DestinationsPlugin.Instance.getCommandManager.registerCommandClass(Commands.class);

        getProcessingClass = new Processing();

        // Mark the version
        if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_8_R3")) {
            getBridge = new MC_1_12_R1();
            Version = 10808;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_9_R1")) {
            getBridge = new MC_1_12_R1();
            Version = 10900;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_9_R2")) {
            getBridge = new MC_1_12_R1();
            Version = 10902;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_10_R1")) {
            getBridge = new MC_1_12_R1();
            Version = 11000;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && Bukkit.getServer().getVersion().endsWith("MC: 1.11)")) {
            getBridge = new MC_1_12_R1();
            Version = 11100;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && Bukkit.getServer().getVersion().endsWith("MC: 1.11.1)")) {
            getBridge = new MC_1_12_R1();
            Version = 11110;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && Bukkit.getServer().getVersion().endsWith("MC: 1.11.2)")) {
            getBridge = new MC_1_12_R1();
            Version = 11120;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_12_R1")) {
            getBridge = new MC_1_12_R1();
            Version = 11200;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_13_R1")) {
            getBridge = new MC_1_13_R2();
            Version = 11300;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_13_R2")) {
            getBridge = new MC_1_13_R2();
            Version = 11310;
        } else {
            return;
        }
    }

    public void logToConsole(String logLine) {
        Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + DestinationsPlugin.Instance.getDescription().getName() + "] " + logLine);
    }

    public void disablePlugin() {

    }

}
