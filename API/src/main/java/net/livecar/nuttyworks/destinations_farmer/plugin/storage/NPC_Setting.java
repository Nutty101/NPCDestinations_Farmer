package net.livecar.nuttyworks.destinations_farmer.plugin.storage;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;

public class NPC_Setting {
	
	public int npcID;
	public HashMap<UUID,Location_Setting> locations;
	public Location currentDestination;
	public Location lastAttempt;
	public CurrentAction currentAction = CurrentAction.IDLE;
	public NPCDestinationsTrait destinationsTrait;
	public Date lastAction;
	public Long blockUntil = 0L;
	
	public NPC_Setting()	
	{		
		locations = new HashMap<UUID,Location_Setting>();
		lastAction = new Date();
	}
	public void setNPC(Integer npcid)
	{
		this.npcID = npcid;
		NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
		destinationsTrait = npc.getTrait(NPCDestinationsTrait.class);
		locations = new HashMap<UUID,Location_Setting>();
	}
	public Integer getNPCID()
	{
		return npcID;
	}
	
	public enum CurrentAction
	{
		IDLE,
		TRAVERSING,
		FARMING,
		ABORTING,
	}
}
