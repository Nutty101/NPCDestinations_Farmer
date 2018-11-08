package net.livecar.nuttyworks.destinations_farmer;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import net.citizensnpcs.Citizens;
import net.livecar.nuttyworks.destinations_farmer.plugin.VersionInterface;
import net.livecar.nuttyworks.destinations_farmer.plugin.MC_1_12_R1;
import net.livecar.nuttyworks.destinations_farmer.plugin.MC_1_13_R2;
import net.livecar.nuttyworks.destinations_farmer.plugin.Commands;
import net.livecar.nuttyworks.destinations_farmer.plugin.PluginExtension;
import net.livecar.nuttyworks.destinations_farmer.storage.Location_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

public class Farmer {
    // For quick reference to this instance of the plugin.
    public static Farmer                  Instance              = null;

    // Links to classes
    public Citizens                       getCitizensPlugin;
    public DestinationsPlugin             getDestinationsPlugin = null;
    public BukkitPlugin                   getPluginReference    = null;
    public VersionInterface               getProcessingClass    = null;
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

        // Mark the version
        if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_8_R3")) {
            getProcessingClass = new MC_1_12_R1();
            Version = 10808;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_9_R1")) {
            getProcessingClass = new MC_1_12_R1();
            Version = 10900;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_9_R2")) {
            getProcessingClass = new MC_1_12_R1();
            Version = 10902;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_10_R1")) {
            getProcessingClass = new MC_1_12_R1();
            Version = 11000;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && Bukkit.getServer().getVersion().endsWith("MC: 1.11)")) {
            getProcessingClass = new MC_1_12_R1();
            Version = 11100;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && Bukkit.getServer().getVersion().endsWith("MC: 1.11.1)")) {
            getProcessingClass = new MC_1_12_R1();
            Version = 11110;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && Bukkit.getServer().getVersion().endsWith("MC: 1.11.2)")) {
            getProcessingClass = new MC_1_12_R1();
            Version = 11120;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_12_R1")) {
            getProcessingClass = new MC_1_12_R1();
            Version = 11200;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_13_R1")) {
            getProcessingClass = new MC_1_13_R2();
            Version = 11300;
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_13_R2")) {
            getProcessingClass = new MC_1_13_R2();
            Version = 11310;
        } else {
            return;
        }
    }

    void getDefaultConfigs() {
        // Create the default folders
        if (!DestinationsPlugin.Instance.getDataFolder().exists())
            DestinationsPlugin.Instance.getDataFolder().mkdirs();
        if (!languagePath.exists())
            languagePath.mkdirs();

        // Validate that the default package is in the MountPackages folder. If
        // not, create it.
        exportConfig(languagePath, "en_def-farmer.yml");

    }

    void exportConfig(File path, String filename) {
        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "nuDestinationsFarmer.exportConfig()|");
        File fileConfig = new File(path, filename);
        if (!fileConfig.isDirectory()) {
            // Reader defConfigStream = null;
            try {
                FileUtils.copyURLToFile((URL) getClass().getResource("/" + filename), fileConfig);
            } catch (IOException e1) {
                if (getDestinationsPlugin != null)
                    DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.SEVERE, "nuDestinationsFarmer.exportConfig()|FailedToExtractFile(" + filename + ")");
                else
                    logToConsole(" Failed to extract default file (" + filename + ")");
                return;
            }
        }
    }

    public void logToConsole(String logLine) {
        Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + DestinationsPlugin.Instance.getDescription().getName() + "] " + logLine);
    }

    public void disablePlugin() {

    }

}
