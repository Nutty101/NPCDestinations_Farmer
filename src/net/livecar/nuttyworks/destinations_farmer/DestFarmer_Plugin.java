package net.livecar.nuttyworks.destinations_farmer;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.livecar.nuttyworks.destinations_farmer.worldguard.WorldGuard_Plugin;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

public class DestFarmer_Plugin extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener {
    
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("NPC_Destinations") == null) {
            Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations2 not found, not registering as plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            if (getServer().getPluginManager().getPlugin("NPC_Destinations").getDescription().getVersion().startsWith("1")) {
                Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations V1 was found, This requires V2. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            } else if (!getServer().getPluginManager().getPlugin("NPC_Destinations").isEnabled()) {
                Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations was found, but was disabled. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            Destinations_Farmer.Instance = new Destinations_Farmer();
            Destinations_Farmer.Instance.getPluginReference = this;
            Destinations_Farmer.Instance.getDestinationsPlugin = DestinationsPlugin.Instance;
            // Force destinations to refresh its language files.
            Destinations_Farmer.Instance.getDestinationsPlugin.getLanguageManager.loadLanguages(true);
        }

        // Global references
        Destinations_Farmer.Instance.getCitizensPlugin = DestinationsPlugin.Instance.getCitizensPlugin;

        // Setup the default paths in the storage folder.
        Destinations_Farmer.Instance.languagePath = new File(DestinationsPlugin.Instance.getDataFolder(), "/Languages/");

        // Generate the default folders and files.
        Destinations_Farmer.Instance.getDefaultConfigs();

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.consoleMessage(this, "farmer", "console_messages.worldguard_notfound");
        } else {
            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.consoleMessage(this, "farmer", "console_messages.worldguard_found", getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion());
            Destinations_Farmer.Instance.getWorldGuardPlugin = new WorldGuard_Plugin();
        }
    }

    public void onDisable() {
        if (this.isEnabled()) {
            if (Destinations_Farmer.Instance != null && Destinations_Farmer.Instance.getDestinationsPlugin != null) {
                Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.CONFIG, "nuDestinationFarmer.onDisable()|Stopping Internal Processes");
            }
            Bukkit.getServer().getScheduler().cancelTasks(this);
        }
    }

    @EventHandler
    public void CitizensLoaded(final CitizensEnableEvent event) {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    Destinations_Farmer.Instance.getProcessingClass.pluginTick();
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                   if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                        Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.logToConsole(Destinations_Farmer.Instance.getPluginReference, "Error:" + sw.toString());
                    else
                        Destinations_Farmer.Instance.logToConsole("Error on farmertick: " + sw.toString());
                }
            }
        }, 1L, 10L);
    }

    @EventHandler
    public void CitizensDisabled(final CitizensDisableEvent event) {
        Bukkit.getServer().getScheduler().cancelTasks(this);
        if (Destinations_Farmer.Instance.getDestinationsPlugin == null) {
            Destinations_Farmer.Instance.logToConsole("Disabled..");
        } else {
            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.consoleMessage(Destinations_Farmer.Instance.getPluginReference, "farmer", "console_messages.plugin_ondisable");
        }
        Destinations_Farmer.Instance = null;
    }
}
