package net.livecar.nuttyworks.destinations_farmer.listeners;

import net.livecar.nuttyworks.destinations_farmer.Destinations_Farmer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandListener implements org.bukkit.event.Listener
{
	
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] inargs) 
	{
		if (inargs.length == 0 || inargs[0].equalsIgnoreCase("help")) 
		{
			sender.sendMessage(ChatColor.GOLD + "--- "+ ChatColor.GREEN  
					+ Destinations_Farmer.Instance.getPluginReference.getDescription().getName() + " Help " 
					+ ChatColor.GOLD + " --------------------- " + ChatColor.WHITE + "V "
					+ Destinations_Farmer.Instance.getPluginReference.getDescription().getVersion());
			return true;
		}
		
		Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.sendMessage("farmer",sender, 
				"messages.invalid_command");
		onCommand(sender,cmd,cmdLabel,new String[]{"help"});
		return true; // do this if you didn't handle the command.
	}
}
