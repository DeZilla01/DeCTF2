package net.dezilla.dectf2.commands.mapmanager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.util.MapManagerWorld;

public class WorldCommand extends Command{

	public WorldCommand() {
		super("world");
		setUsage("/world <world>");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"You must be a player to use this command.");
			return false;
		}
		Player p = (Player) sender;
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED+"Usage: "+getUsage());
			return false;
		}
		World w = Bukkit.getWorld(args[0]);
		if(w == null) {
			sender.sendMessage(ChatColor.RED+"Invalid World.");
			return false;
		}
		MapManagerWorld world = MapManagerWorld.get(w);
		if(world != null && world.getSpawnLocation() != null)
			p.teleport(world.getSpawnLocation());
		else
			p.teleport(w.getSpawnLocation());
		sender.sendMessage("Teleported to "+w.getName());
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		if(args.length==1) {
			for(World world : Bukkit.getWorlds()) {
				String s = ""+world.getName();
				if(s.toLowerCase().startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		return list;
	}

}
