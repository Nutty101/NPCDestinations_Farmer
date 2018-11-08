package net.livecar.nuttyworks.destinations_farmer.storage;

import java.util.UUID;

public class Location_Setting 
{
	public UUID locationID;
	public String regionName;
	public int maxDistance;
	public boolean plantExisting = false;
	public boolean blocking = false;
	public Long blockUntil = 0L;
}
