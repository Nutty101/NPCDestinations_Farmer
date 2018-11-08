package net.livecar.nuttyworks.destinations_farmer.plugin;

import org.bukkit.Location;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting;

public interface VersionInterface {
    public void pluginTick();
    public Location locateNPCWork(NPC_Setting farmSet, Location sourceLocation, String regionName, NPC npc);
}
