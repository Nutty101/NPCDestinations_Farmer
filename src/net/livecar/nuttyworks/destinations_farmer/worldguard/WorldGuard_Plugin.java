package net.livecar.nuttyworks.destinations_farmer.worldguard;

import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WorldGuard_Plugin 
{
	public WorldGuard_Plugin()
	{
		
	}
	public WorldGuardPlugin getWGPlugin()
	{
		return WGBukkit.getPlugin();
	}
	public RegionManager getRegionManager(World world)
	{
		return WGBukkit.getPlugin().getRegionManager(world);
	}
}
