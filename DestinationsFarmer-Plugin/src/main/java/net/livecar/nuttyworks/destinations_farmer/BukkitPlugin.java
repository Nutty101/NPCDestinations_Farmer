package net.livecar.nuttyworks.destinations_farmer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

public class BukkitPlugin extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener {

    private Farmer  farmerRef;

    public void onEnable() {

        if (getServer().getPluginManager().getPlugin("NPC_Destinations") == null) {
            Bukkit.getLogger().log(Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations2 not found, not registering as plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            String[] versionParts = getServer().getPluginManager().getPlugin("NPC_Destinations").getDescription().getVersion().split("\\.");
            if (versionParts[0].equalsIgnoreCase("1")) {
                Bukkit.getLogger().log(Level.SEVERE, "[" + getDescription().getName() + "] " + "NPCDestinations V1 was found, This requires V2. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            } else if (versionParts.length < 3) {
                Bukkit.getLogger().log(Level.SEVERE, "[" + getDescription().getName() + "] " + "NPCDestinations older than 2.3.2 was found, please update to the latest. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            } else if (versionParts[0].equals("2") && Integer.parseInt(versionParts[1]) <= 3 && Integer.parseInt(versionParts[2]) < 2) {
                Bukkit.getLogger().log(Level.SEVERE, "[" + getDescription().getName() + "] " + "NPCDestinations older than 2.3.2 was found, please update to the latest. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            } else if (!getServer().getPluginManager().getPlugin("NPC_Destinations").isEnabled()) {
                Bukkit.getLogger().log(Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations was found, but was disabled. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            farmerRef = new Farmer();
            farmerRef.getPluginReference = this;
            // Force destinations to refresh its language files.
            DestinationsPlugin.Instance.getLanguageManager.loadLanguages(true);
        }

        // Global references
        farmerRef.getCitizensPlugin = DestinationsPlugin.Instance.getCitizensPlugin;

        // Setup the default paths in the storage folder.
        farmerRef.languagePath = new File(DestinationsPlugin.Instance.getDataFolder(), "/Languages/");

        // Generate the default folders and files.
        getDefaultConfigs();

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        if (this.isEnabled()) {
            if (farmerRef != null) {
                DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.CONFIG, "nuDestinationFarmer.onDisable()|Stopping Internal Processes");
            }
            farmerRef.disablePlugin();
            Bukkit.getServer().getScheduler().cancelTasks(this);

        }
    }

    @EventHandler
    public void CitizensLoaded(final CitizensEnableEvent event) {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    farmerRef.getProcessingClass.onPluginTick();
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    DestinationsPlugin.Instance.getMessageManager.logToConsole(farmerRef.getPluginReference, "Error:" + sw.toString());
                }
            }
        }, 10L, farmerRef.getDefaultConfig.getLong("tick-interval", 5L));
    }

    @EventHandler
    public void CitizensDisabled(final CitizensDisableEvent event) {
        Bukkit.getServer().getScheduler().cancelTasks(this);
        DestinationsPlugin.Instance.getMessageManager.consoleMessage(farmerRef.getPluginReference, "farmer", "console_messages.plugin_ondisable");
        farmerRef = null;
    }

    void getDefaultConfigs() {
        // Create the default folders
        if (!DestinationsPlugin.Instance.getDataFolder().exists())
            DestinationsPlugin.Instance.getDataFolder().mkdirs();
        if (!farmerRef.languagePath.exists())
            farmerRef.languagePath.mkdirs();

        if (!(new File(getDataFolder(), "config.yml").exists()))
            exportConfig(getDataFolder(), "config.yml");
        
        // Validate that the default package is in the MountPackages folder. If
        // not, create it.
        farmerRef.getDefaultConfig = DestinationsPlugin.Instance.getUtilitiesClass.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
        exportConfig(farmerRef.languagePath, "en_def-farmer.yml");

    }

    void exportConfig(File path, String filename) {
        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "nuDestinationsFarmer.exportConfig()|");
        File fileConfig = new File(path, filename);
        if (!fileConfig.isDirectory()) {
            // Reader defConfigStream = null;
            try {
                FileUtils.copyURLToFile((URL) getClass().getResource("/" + filename), fileConfig);
            } catch (IOException e1) {
                DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.SEVERE, "nuDestinationsFarmer.exportConfig()|FailedToExtractFile(" + filename + ")");
                return;
            }
        }
    }
}
